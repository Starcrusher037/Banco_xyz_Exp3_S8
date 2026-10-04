package cl.duoc.auth_server.services;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de la construccion, emision y firma de tokens JSON Web Token (JWT).
 * 
 * Utiliza el codificador NimbusJwtEncoder configurado con el par de claves RSA (RS256).
 * Define los metadatos y claims estandar del token:
 * - 'iss' (Issuer): Identifica al servidor emisor de confianza (auth-server).
 * - 'iat' (Issued At): Momento exacto de creacion del token.
 * - 'exp' (Expiration Time): Tiempo de expiracion configurable (por defecto 3600 segundos).
 * - 'sub' (Subject): Nombre de usuario unico autenticado en GitHub.
 * - 'provider': Proveedor de identidad federado ('github').
 * - 'scope': Permisos autorizados ('transacciones.read transacciones.write') para operar en el core bancario.
 * - Claims opcionales: Correo electronico y nombre completo del usuario.
 */
@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long expirationSeconds;

    /**
     * Inyeccion de dependencias por constructor.
     * 
     * @param jwtEncoder Codificador Nimbus configurado con la clave RSA privada.
     * @param issuer URL identificatoria del emisor configurada en properties o fallback.
     * @param expirationSeconds Tiempo de validez del token en segundos.
     */
    public JwtTokenService(
            JwtEncoder jwtEncoder,
            @Value("${ms.auth.jwt.issuer:http://localhost:8080}") String issuer,
            @Value("${ms.auth.jwt.expiration-seconds:3600}") long expirationSeconds) {

        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.expirationSeconds = expirationSeconds;
    }

    /**
     * Construye y firma digitalmente un nuevo token de acceso JWT para el usuario autenticado.
     * 
     * @param githubLogin Nombre de usuario extraido del perfil de GitHub (usado como Subject).
     * @param email Correo electronico del usuario (si esta disponible publicamente en GitHub).
     * @param name Nombre personal del usuario registrado en GitHub.
     * @return Cadena de texto compacta representando el token JWT firmado en formato Base64URL.
     */
    public String generateToken(String githubLogin, String email, String name) {
        Instant now = Instant.now();

        // Construccion del conjunto de claims estandar y personalizados
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .subject(githubLogin)
                .claim("provider", "github")
                .claim("scope", "transacciones.read transacciones.write");

        if (email != null && !email.isEmpty()) {
            claimsBuilder.claim("email", email);
        }

        if (name != null && !name.isEmpty()) {
            claimsBuilder.claim("name", name);
        }

        JwtClaimsSet claims = claimsBuilder.build();

        // Firma y codificacion del token con la clave RSA privada
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

}
