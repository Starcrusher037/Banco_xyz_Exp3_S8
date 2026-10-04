package cl.duoc.auth_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

import cl.duoc.auth_server.security.OAuth2LoginSuccessHandler;

/**
 * Configuracion central de seguridad web y proteccion de rutas para el servidor de autenticacion.
 * 
 * Define las politicas del filtro de seguridad (SecurityFilterChain) que gestionan:
 * 1. Rutas publicas de libre acceso (inicio, endpoints informativos de autenticacion,
 *    flujos de redireccion OAuth2 y el endpoint JWKS de claves publicas).
 * 2. Integracion del flujo OAuth 2.0 Login con GitHub.
 * 3. Enlace con el manejador personalizado (OAuth2LoginSuccessHandler) que procesa
 *    el consentimiento del usuario e intercepta el resultado para emitir el JWT.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final OAuth2LoginSuccessHandler successHandler;

  /**
   * Inyeccion de dependencias por constructor del manejador de exito en login OAuth2.
   * 
   * @param successHandler Componente que emite el JWT tras autenticar contra GitHub.
   */
  public SecurityConfig(OAuth2LoginSuccessHandler successHandler) {
    this.successHandler = successHandler;
  }

  /**
   * Configura la cadena de filtros de seguridad HTTP de Spring Security.
   * 
   * - Deshabilita CSRF dado que el servicio opera como API REST y emisor de tokens sin estado de sesion web tradicional.
   * - Configura las reglas de autorizacion HTTP:
   *   * Permite acceso sin credenciales a "/", "/api/auth/**", rutas de handshake OAuth2 ("/oauth2/**", "/login/**"),
   *     el endpoint de claves publicas "/.well-known/jwks.json" y chequeos de salud Actuator.
   *   * Exige autenticacion para cualquier otra solicitud entrante.
   * - Activa el inicio de sesion mediante OAuth 2.0 (oauth2Login) delegando la captura
   *   del usuario autenticado en el successHandler personalizado.
   * 
   * @param http Constructor HttpSecurity de Spring Security.
   * @return SecurityFilterChain configurada y compilada.
   * @throws Exception Si ocurre algun error en la definicion de la cadena de filtros.
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http)
      throws Exception {

    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(
                "/",
                "/api/auth/**",
                "/oauth2/**",
                "/login/**",
                "/.well-known/jwks.json",
                "/actuator/health",
                "/actuator/info")
            .permitAll()
            .anyRequest().authenticated())
        .oauth2Login(oauth2 -> oauth2
            .successHandler(successHandler))
        .logout(Customizer.withDefaults());

    return http.build();
  }

}
