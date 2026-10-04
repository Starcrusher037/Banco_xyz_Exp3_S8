package cl.duoc.bancoxyz.mensajeria.mensajeria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ReceptorTransacciones {

    private static final Logger log = LoggerFactory.getLogger(ReceptorTransacciones.class);
    private final List<EventoTransaccion> eventosRecibidos = new CopyOnWriteArrayList<>();

    @JmsListener(destination = "transacciones.bancarias")
    public void recibir(EventoTransaccion evento) {
        log.info("===============================================================");
        log.info("[MS-MENSAJERIA] EVENTO RECIBIDO DESDE ACTIVEMQ");
        log.info("ID Transaccion: {}", evento.getTransaccionId());
        log.info("Tipo Operacion: {}", evento.getTipoOperacion());
        log.info("Canal:          {}", evento.getCanal());
        log.info("Monto:          ${}", evento.getMonto());
        log.info("Cuenta Origen:  {}", evento.getCuentaOrigenId());
        log.info("Cuenta Destino: {}", evento.getCuentaDestinoId());
        log.info("Estado:         {}", evento.getEstado());
        log.info("Fecha/Hora:     {}", evento.getFechaHora());
        log.info("Detalle:        {}", evento.getDetalle());
        log.info("===============================================================");

        procesarEvento(evento);
    }

    private void procesarEvento(EventoTransaccion evento) {
        log.info("[MS-MENSAJERIA] Procesando notificacion de transaccion: {}", evento.getTransaccionId());
        eventosRecibidos.add(evento);
    }

    public List<EventoTransaccion> getEventosRecibidos() {
        return Collections.unmodifiableList(eventosRecibidos);
    }
}
