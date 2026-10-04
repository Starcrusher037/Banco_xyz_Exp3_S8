package cl.duoc.discovery_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Configuracion de Seguridad para Eureka Server.
 * 
 * Permite autenticacion HTTP Basic para la consola y clientes, y desactiva CSRF en /eureka/**
 * para que los microservicios clientes (core-service, auth-server, ms-mensajeria, etc.) puedan enviar sus
 * peticiones de registro y senales de vida (heartbeats) sin ser bloqueados.
 */
@Configuration 
@EnableWebSecurity 
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/eureka/**").authenticated()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(withDefaults())
            .csrf(csrf -> csrf.ignoringRequestMatchers("/eureka/**"));
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.withUsername("admin")
                .password("{noop}eureka2026")
                .roles("ADMIN")
                .build();
        UserDetails eureka = User.withUsername("eureka")
                .password("{noop}eureka2026")
                .roles("CLIENT")
                .build();
        return new InMemoryUserDetailsManager(admin, eureka);
    }
}
