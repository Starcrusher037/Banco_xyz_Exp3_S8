package cl.duoc.auth_server.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

import cl.duoc.auth_server.services.JwtTokenService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Manejador personalizado de exito para el flujo de autenticacion OAuth 2.0 con GitHub.
 * 
 * Intercepta la peticion una vez que el proveedor de identidad externo (GitHub) ha validado
 * las credenciales y el consentimiento del usuario, retornando el codigo de autorizacion al callback
 * '/login/oauth2/code/github'.
 * 
 * Flujo de operacion:
 * 1. Extrae el principal autenticado (OAuth2User) con los atributos de perfil de GitHub (login, email, nombre).
 * 2. Invoca a JwtTokenService para generar un token de acceso JWT autofirmado con clave privada RSA.
 * 3. Construye un cuerpo de respuesta JSON estandarizado conteniendo el Bearer Token y sus metadatos.
 * 4. Escribe directamente la respuesta HTTP 200 OK con Content-Type application/json hacia el cliente.
 */
@Component 
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtTokenService jwtTokenService;
  private final ObjectMapper objectMapper;

  /**
   * Inyeccion de dependencias por constructor.
   * 
   * @param jwtTokenService Servicio encargado de la emision y firma criptografica del JWT.
   * @param objectMapper Utilidad de Jackson para la serializacion del objeto de respuesta a JSON.
   */
  public OAuth2LoginSuccessHandler(JwtTokenService jwtTokenService, ObjectMapper objectMapper) {
    this.jwtTokenService = jwtTokenService;
    this.objectMapper = objectMapper;
  }

  /**
   * Metodo ejecutado automaticamente por Spring Security tras autenticar exitosamente en GitHub.
   * 
   * @param request Peticion HTTP entrante con los parametros de callback de GitHub.
   * @param response Respuesta HTTP en la que se inyecta el JSON con el token de acceso.
   * @param authentication Objeto de autenticacion que contiene los datos del usuario de GitHub.
   * @throws IOException Si ocurre un error de entrada/salida al escribir en el flujo de respuesta.
   * @throws ServletException Si ocurre un error durante el procesamiento del servlet.
   */
  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    // Conversion del objeto de autenticacion al token OAuth2 especifico
    OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

    // Obtencion del perfil de usuario suministrado por la API de GitHub
    OAuth2User githubUser = oauthToken.getPrincipal();

    String githubLogin = githubUser.getAttribute("login");
    String email = githubUser.getAttribute("email");
    String name = githubUser.getAttribute("name");

    // Generacion del JWT con firma asimetrica RSA RS256
    String jwt = jwtTokenService.generateToken(
        githubLogin,
        email,
        name);

    // Construccion de la estructura de respuesta que consumira el cliente o frontend
    Map<String, Object> tokenResponse = new LinkedHashMap<>();
    tokenResponse.put("tokenType", "Bearer");
    tokenResponse.put("accessToken", jwt);
    tokenResponse.put("expiresIn", 3600);
    tokenResponse.put("githubUser", githubLogin);
    tokenResponse.put("email", email);

    // Configuracion de cabeceras de respuesta HTTP
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    // Serializacion del JSON en el flujo de salida
    objectMapper.writeValue(
        response.getOutputStream(),
        tokenResponse);
  }

}
