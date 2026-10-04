package cl.duoc.auth_server.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Map;
import com.nimbusds.jose.jwk.JWKSet;

/**
 * Controlador REST encargado de exponer el conjunto de claves web JSON (JWKS) publicas.
 * 
 * Cumple con el estandar RFC 7517 exponiendo la ruta reconocida internacionalmente:
 * '/.well-known/jwks.json'.
 * 
 * Rol en la arquitectura distribuida:
 * Permite a los Servidores de Recursos (Resource Servers, como core-service) consultar
 * la clave publica RSA para validar la firma criptografica de los tokens Bearer JWT
 * recibidos en las cabeceras HTTP de las peticiones de los usuarios.
 * Gracias a este endpoint, los microservicios validan tokens localmente sin acoplamiento
 * a la base de datos ni transmision de claves secretas.
 */
@RestController 
public class JwkSetController {

  private final JWKSet publicJwkSet;

  /**
   * Inyeccion de dependencias del conjunto de claves publicas RSA.
   * 
   * @param publicJwkSet Bean configurado en JwtKeyConfig conteniendo unicamente la clave publica.
   */
  public JwkSetController(JWKSet publicJwkSet) {
    this.publicJwkSet = publicJwkSet;
  }

  /**
   * Endpoint publico que retorna el JWK Set en formato JSON.
   * 
   * Contiene los parametros de la clave publica RSA (modulo 'n', exponente 'e',
   * algoritmo 'kty' y el identificador 'kid').
   * 
   * @return Mapa de datos JSON serializado que representa el conjunto JWK.
   */
  @GetMapping("/.well-known/jwks.json")
  public Map<String, Object> getPublicKeys() {
    return publicJwkSet.toJSONObject();
  }

}
