package cl.duoc.bancoxyz.mensajeria.controller;

import cl.duoc.bancoxyz.mensajeria.mensajeria.EventoTransaccion;
import cl.duoc.bancoxyz.mensajeria.mensajeria.ReceptorTransacciones;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mensajeria")
@Tag(name = "Mensajería Bancaria", description = "Endpoints para consulta de eventos y notificaciones recibidas vía JMS")
public class ControladorMensajeria {

    private final ReceptorTransacciones receptor;

    public ControladorMensajeria(ReceptorTransacciones receptor) {
        this.receptor = receptor;
    }

    @Operation(summary = "Listar eventos consumidos desde la cola ActiveMQ")
    @GetMapping("/eventos")
    public ResponseEntity<Map<String, Object>> obtenerEventos() {
        List<EventoTransaccion> eventos = receptor.getEventosRecibidos();
        return ResponseEntity.ok(Map.of(
                "totalEventos", eventos.size(),
                "colaJms", "transacciones.bancarias",
                "broker", "Apache ActiveMQ Classic (tcp://localhost:61616)",
                "eventos", eventos
        ));
    }
}
