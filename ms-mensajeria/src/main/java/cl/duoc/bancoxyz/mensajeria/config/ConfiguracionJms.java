package cl.duoc.bancoxyz.mensajeria.config;

import cl.duoc.bancoxyz.mensajeria.mensajeria.EventoTransaccion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.JacksonJsonMessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ConfiguracionJms {

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put("EventoTransaccion", EventoTransaccion.class);
        converter.setTypeIdMappings(typeIdMappings);

        return converter;
    }
}
