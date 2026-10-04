package cl.duoc.bancoxyz.core.service;

import cl.duoc.bancoxyz.core.model.Cuenta;
import cl.duoc.bancoxyz.core.model.MovimientoAnual;
import cl.duoc.bancoxyz.core.model.Transaccion;
import cl.duoc.bancoxyz.core.repository.BancoRepository;
import cl.duoc.bancoxyz.core.mensajeria.EventoTransaccion;
import cl.duoc.bancoxyz.core.mensajeria.PublicadorTransacciones;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class BancoService {

    private final BancoRepository bancoRepository;
    private final PublicadorTransacciones publicadorTransacciones;

    public BancoService(BancoRepository bancoRepository, PublicadorTransacciones publicadorTransacciones) {
        this.bancoRepository = bancoRepository;
        this.publicadorTransacciones = publicadorTransacciones;
    }

    public Cuenta obtenerCuentaPorId(Long cuentaId) {
        return bancoRepository.buscarCuentaPorId(cuentaId)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la cuenta con ID: " + cuentaId));
    }

    public List<Cuenta> obtenerTodasLasCuentas() {
        return bancoRepository.obtenerTodasLasCuentas();
    }

    public List<Transaccion> obtenerTransaccionesPorCuenta(Long cuentaId) {
        obtenerCuentaPorId(cuentaId);
        return bancoRepository.buscarTransaccionesPorCuenta(cuentaId);
    }

    public List<MovimientoAnual> obtenerMovimientosAnualesPorCuenta(Long cuentaId) {
        obtenerCuentaPorId(cuentaId);
        return bancoRepository.buscarMovimientosAnualesPorCuenta(cuentaId);
    }

    public synchronized Cuenta ejecutarRetiro(Long cuentaId, Long monto, String canal) {
        Cuenta cuenta = obtenerCuentaPorId(cuentaId);
        long saldoTotalDisponible = cuenta.getSaldoContable() + cuenta.getLineaSobregiro();

        if (monto > saldoTotalDisponible) {
            throw new IllegalArgumentException("Saldo insuficiente para efectuar el retiro");
        }

        cuenta.setSaldoContable(cuenta.getSaldoContable() - monto);
        bancoRepository.guardarCuenta(cuenta);

        Transaccion tx = new Transaccion(
                null,
                cuentaId,
                LocalDate.now(),
                monto,
                "debito",
                "Giro transaccional en " + canal,
                canal
        );
        bancoRepository.guardarTransaccion(tx);

        // Publicacion Asincrona del Evento Transaccional a ActiveMQ con tolerancia a fallos
        EventoTransaccion evento = EventoTransaccion.builder()
                .transaccionId("TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .cuentaOrigenId(cuentaId)
                .cuentaDestinoId(null)
                .monto(monto)
                .tipoOperacion("RETIRO")
                .canal(canal != null ? canal : "ATM")
                .estado("EXITOSA")
                .fechaHora(LocalDateTime.now().toString())
                .detalle("Giro por canal " + canal + " completado exitosamente")
                .build();
        publicadorTransacciones.publicarTransaccion(evento);

        return cuenta;
    }

    public synchronized void ejecutarTransferencia(Long cuentaOrigenId, Long cuentaDestinoId, Long monto, String comentario) {
        Cuenta origen = obtenerCuentaPorId(cuentaOrigenId);
        Cuenta destino = obtenerCuentaPorId(cuentaDestinoId);

        long disponible = origen.getSaldoContable() + origen.getLineaSobregiro();
        if (monto > disponible) {
            throw new IllegalArgumentException("Saldo insuficiente en cuenta origen");
        }

        origen.setSaldoContable(origen.getSaldoContable() - monto);
        destino.setSaldoContable(destino.getSaldoContable() + monto);

        bancoRepository.guardarCuenta(origen);
        bancoRepository.guardarCuenta(destino);

        Transaccion cargo = new Transaccion(null, cuentaOrigenId, LocalDate.now(), monto, "debito",
                "Transferencia a cta " + cuentaDestinoId + ": " + (comentario != null ? comentario : "Traspaso"), "MOVIL");
        Transaccion abono = new Transaccion(null, cuentaDestinoId, LocalDate.now(), monto, "credito",
                "Transferencia de cta " + cuentaOrigenId + ": " + (comentario != null ? comentario : "Abono"), "MOVIL");

        bancoRepository.guardarTransaccion(cargo);
        bancoRepository.guardarTransaccion(abono);

        // Publicacion Asincrona del Evento Transaccional a ActiveMQ con tolerancia a fallos
        EventoTransaccion evento = EventoTransaccion.builder()
                .transaccionId("TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .cuentaOrigenId(cuentaOrigenId)
                .cuentaDestinoId(cuentaDestinoId)
                .monto(monto)
                .tipoOperacion("TRANSFERENCIA")
                .canal("MOVIL")
                .estado("EXITOSA")
                .fechaHora(LocalDateTime.now().toString())
                .detalle(comentario != null ? comentario : "Transferencia de fondos exitosa")
                .build();
        publicadorTransacciones.publicarTransaccion(evento);
    }
}
