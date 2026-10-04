package cl.duoc.auth_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de arranque para el microservicio de autenticacion y autorizacion (auth-server).
 * 
 * Responsabilidades del microservicio dentro del ecosistema distribuido:
 * 1. Actuar como cliente OAuth 2.0 federado con el proveedor de identidad externo GitHub.
 * 2. Gestionar la generacion de pares de claves asimetricas RSA para la firma de tokens (RS256).
 * 3. Emitir tokens de acceso JWT (JSON Web Tokens) tras la autorizacion satisfactoria del usuario.
 * 4. Exponer el endpoint publico '/.well-known/jwks.json' para permitir que los Servidores de Recursos
 *    (Resource Servers) validen de manera descentralizada la autenticidad e integridad de los tokens.
 * 5. Registrarse en el servidor de descubrimiento Eureka para la resolucion de nombres e integracion cloud.
 */
@SpringBootApplication
public class AuthServerApplication {

	/**
	 * Punto de entrada principal que inicializa el contexto de Spring Boot y el servidor embebido.
	 * 
	 * @param args Argumentos de linea de comandos pasados a la aplicacion.
	 */
	public static void main(String[] args) {
		SpringApplication.run(AuthServerApplication.class, args);
	}

}
