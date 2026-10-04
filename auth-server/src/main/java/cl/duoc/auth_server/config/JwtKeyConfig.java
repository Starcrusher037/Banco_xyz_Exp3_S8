package cl.duoc.auth_server.config;

import java.security.interfaces.RSAPublicKey;
import java.util.UUID;
import java.security.interfaces.RSAPrivateKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;


/**
 * Configuración para la gestión de claves asimetricas RSA y emision de tokens JWT.
 * 
 * Esta clase se encarga de:
 * 1. Generar un par de claves asimetricas RSA de 2048 bits (clave privada y clave publica).
 * 2. Mantener la clave privada dentro del servidor de autenticacion para firmar digitalmente
 *    los tokens de acceso (algoritmo RS256).
 * 3. Configurar el conjunto de claves web JSON (JWKS - JSON Web Key Set) publico, permitiendo
 *    que los microservicios consumidores (como core-service) puedan validar la firma de los
 *    tokens de forma descentralizada sin necesidad de compartir secretos simetricos.
 */
@Configuration 
public class JwtKeyConfig {

  /**
   * Genera y registra el bean de la clave RSA completa (par publico/privado).
   * Se le asigna un identificador unico (KeyID) mediante UUID para rastrear la clave
   * en la cabecera 'kid' de los tokens JWT emitidos.
   * 
   * @return Objeto RSAKey con la clave publica, clave privada y su respectivo KeyID.
   */
  @Bean
  public RSAKey rsaKey() {
    KeyPair keyPair = generateKeyPair();

    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
    RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

    return new RSAKey.Builder(publicKey)
        .privateKey(privateKey)
        .keyID(UUID.randomUUID().toString())
        .build();
  }

  /**
   * Define la fuente de claves JWK (JWKSource) requerida por el codificador Nimbus.
   * Encapsula el conjunto de claves en una coleccion inmutable para su uso durante
   * el proceso de firma de tokens.
   * 
   * @param rsaKey Par de claves RSA generado en el sistema.
   * @return Fuente JWKSource protegida para el contexto de seguridad.
   */
  @Bean
  public JWKSource<SecurityContext> jwkSource(RSAKey rsaKey) {
    JWKSet jwkSet = new JWKSet(rsaKey);
    return new ImmutableJWKSet<>(jwkSet);
  }

  /**
   * Expone unicamente la parte publica del par de claves RSA (sin la clave privada).
   * Este bean es utilizado por JwkSetController para disponibilizar el endpoint publico
   * '/.well-known/jwks.json', permitiendo a los Resource Servers descargar el certificado.
   * 
   * @param rsaKey Par de claves RSA completo.
   * @return JWKSet conteniendo exclusivamente la clave publica.
   */
  @Bean
  public JWKSet publicJwkSet(RSAKey rsaKey) {
    return new JWKSet(rsaKey.toPublicJWK());
  }

  /**
   * Provee el componente JwtEncoder basado en la libreria Nimbus.
   * Este componente es inyectado en JwtTokenService para codificar y firmar digitalmente
   * los claims y cabeceras de cada token JWT generado tras una autenticacion exitosa.
   * 
   * @param jwkSource Fuente de claves que suministra la clave privada para la firma.
   * @return Instancia de NimbusJwtEncoder lista para emision de tokens.
   */
  @Bean
  public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
    return new NimbusJwtEncoder(jwkSource);
  }  

  /**
   * Genera un nuevo par de claves RSA en memoria con una longitud de 2048 bits.
   * Esta longitud garantiza un estandar de seguridad robusto para cifrado asimetrico.
   * 
   * @return Par de claves KeyPair (Public and Private).
   */
  private KeyPair generateKeyPair() {
    try {
      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
      keyPairGenerator.initialize(2048);
      return keyPairGenerator.generateKeyPair();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Error al inicializar el generador de claves RSA: algoritmo no disponible", e);
    }
  }

}
