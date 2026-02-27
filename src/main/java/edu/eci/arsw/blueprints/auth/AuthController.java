package edu.eci.arsw.blueprints.auth;



import edu.eci.arsw.blueprints.security.InMemoryUserService;
import edu.eci.arsw.blueprints.security.RsaKeyProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Instant;
import java.util.Map;

@Tag(name = "Autenticación", description = "Endpoint de login para obtener un token JWT (OAuth 2.0)")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtEncoder encoder;
    private final InMemoryUserService userService;
    private final RsaKeyProperties props;

    public AuthController(JwtEncoder encoder, InMemoryUserService userService, RsaKeyProperties props) {
        this.encoder = encoder;
        this.userService = userService;
        this.props = props;
    }

    @Schema(description = "Credenciales de inicio de sesión")
    public record LoginRequest(
            @Schema(description = "Nombre de usuario", example = "student")
            String username,
            @Schema(description = "Contraseña del usuario", example = "student123")
            String password) {}

    @Schema(description = "Respuesta con el token de acceso JWT")
    public record TokenResponse(
            @Schema(description = "Token JWT firmado con RS256", example = "eyJhbGciOiJSUzI1NiJ9...")
            String access_token,
            @Schema(description = "Tipo de token", example = "Bearer")
            String token_type,
            @Schema(description = "Tiempo de vida del token en segundos", example = "3600")
            long expires_in) {}

    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica al usuario con sus credenciales y retorna un token JWT firmado con RS256. "
                    + "El token incluye los scopes `blueprints.read` y `blueprints.write` y debe enviarse "
                    + "en el header `Authorization: Bearer <token>` para acceder a los endpoints protegidos."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso — token JWT emitido",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "access_token": "eyJhbGciOiJSUzI1NiJ9...",
                                      "token_type": "Bearer",
                                      "expires_in": 3600
                                    }"""))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "error": "invalid_credentials"
                                    }""")))
    })
    @SecurityRequirements  // Este endpoint NO requiere token
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciales del usuario",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "username": "student",
                                      "password": "student123"
                                    }""")))
            @RequestBody LoginRequest req) {
        if (!userService.isValid(req.username(), req.password())) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_credentials"));
        }

        Instant now = Instant.now();
        long ttl = props.tokenTtlSeconds() != null ? props.tokenTtlSeconds() : 3600;
        Instant exp = now.plusSeconds(ttl);

        String scope = "blueprints.read blueprints.write";

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.issuer())
                .issuedAt(now)
                .expiresAt(exp)
                .subject(req.username())
                .claim("scope", scope)
                .build();

        JwsHeader jws = JwsHeader.with(() -> "RS256").build();
        String token = this.encoder.encode(JwtEncoderParameters.from(jws, claims)).getTokenValue();

        return ResponseEntity.ok(new TokenResponse(token, "Bearer", ttl));
    }
}
