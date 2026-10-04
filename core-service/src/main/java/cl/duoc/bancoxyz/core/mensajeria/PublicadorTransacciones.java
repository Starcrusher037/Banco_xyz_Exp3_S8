package cl.duoc.bancoxyz.core.mensajeria;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class PublicadorTransacciones {

    private static final Logger log = LoggerFactory.getLogger(PublicadorTransacciones.class);
    private static final String COLA_DESTINO = "transacciones.bancarias";

    @Autowired
    private JmsTemplate jmsTemplate;

    // Bitacora de contingencia local en memoria ante caida del broker ActiveMQ o Circuito Abierto
    private final List<EventoTransaccion> transaccionesContingencia = new CopyOnWriteArrayList<>();

    @CircuitBreaker(name = "envioMensajeria", fallbackMethod = "fallbackEnvioMensaje")
    @Retry(name = "envioMensajeria", fallbackMethod = "fallbackEnvioMensaje")
    public void publicarTransaccion(EventoTransaccion evento) {
        try {
            log.info("[PUBLICADOR-JMS] Publicando evento transaccional '{}' en la cola '{}'", 
                    evento.getTransaccionId(), COLA_DESTINO);
            jmsTemplate.convertAndSend(COLA_DESTINO, evento);
            log.info("[PUBLICADOR-JMS] Transaccion '{}' publicada con exito en broker ActiveMQ", evento.getTransaccionId());
        } catch (Exception e) {
            fallbackEnvioMensaje(evento, e);
        }
    }

    // Metodo de Fallback cuando el broker ActiveMQ esta caido o el circuito esta ABIERTO
    public void fallbackEnvioMensaje(EventoTransaccion evento, Throwable ex) {
        log.error("[FALLBACK RESILIENCE4J] ActiveMQ no disponible o circuito ABIERTO. Causa: {}", ex.getMessage());
        evento.setEstado("CONTINGENCIA_PENDIENTE_BROKER");
        evento.setDetalle("Broker ActiveMQ inaccesible - Contingencia local: " + ex.getMessage());
        transaccionesContingencia.add(evento);
        log.warn("[FALLBACK RESILIENCE4J] Guardando evento '{}' en contingencia local. Total pendientes: {}", 
                evento.getTransaccionId(), transaccionesContingencia.size());
    }

    public List<EventoTransaccion> getTransaccionesContingencia() {
        return Collections.unmodifiableList(transaccionesContingencia);
    }
}
