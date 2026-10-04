package cl.duoc.auth_server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Pruebas de integracion y carga del contexto para el servidor de autenticacion.
 * 
 * Verifica que todos los beans de seguridad, configuracion criptografica RSA,
 * codificadores JWT y controladores se instancien correctamente sin conflictos de dependencias.
 */
@SpringBootTest
class AuthServerApplicationTests {

	/**
	 * Verifica la inicializacion exitosa del ApplicationContext de Spring Boot.
	 */
	@Test
	void contextLoads() {
	}

}
