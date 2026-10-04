package cl.duoc.auth_server.controllers;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import java.util.Map;

/**
 * Controlador informativo para la gestion del flujo de autenticacion.
 * 
 * Expone un endpoint orientativo para clientes, desarrolladores o herramientas
 * de exploracion API (como Postman o cURL) que consulten la ruta base de autenticacion,
 * indicando cual es el punto de entrada oficial para iniciar el flujo de autorizacion
 * federado con GitHub OAuth 2.0.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  /**
   * Endpoint informativo de inicio de sesion.
   * 
   * Retorna una respuesta JSON explicativa indicando la URL exacta a la que debe
   * dirigirse el navegador o cliente para iniciar el handshake de autorizacion con GitHub.
   * 
   * @return ResponseEntity conteniendo mensaje explicativo y la URL de redireccion a GitHub.
   */
  @GetMapping("/login")
  public ResponseEntity<Map<String, String>> login() {
    return ResponseEntity.ok(
      Map.of(
        "message", "Para autenticar con GitHub debe ingresar a /oauth2/authorization/github",
        "authorizationUrl", "http://localhost:8080/oauth2/authorization/github"
      ));
  }

}
