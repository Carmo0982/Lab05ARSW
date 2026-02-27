package edu.eci.arsw.blueprints.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info()
                        .title("BluePrints API")
                        .version("2.0")
                        .description("""
                                API REST para gestión de planos (blueprints) protegida con **JWT (OAuth 2.0)**.

                                ## Flujo de autenticación
                                1. Realizar `POST /auth/login` con credenciales válidas.
                                2. Copiar el `access_token` de la respuesta.
                                3. Pulsar el botón **Authorize** 🔒 e ingresar: `Bearer <access_token>`.
                                4. Todos los endpoints protegidos enviarán el token automáticamente.

                                ## Usuarios de prueba
                                | Usuario     | Contraseña    | Scopes                                  |
                                |-------------|---------------|------------------------------------------|
                                | `student`   | `student123`  | `blueprints.read`, `blueprints.write`    |
                                | `assistant` | `assistant123`| `blueprints.read`, `blueprints.write`    |
                                """)
                        .contact(new Contact()
                                .name("Jacobo Diaz & Santiago Carmona")
                                .url("https://github.com/DECSIS-ECI"))
                        .license(new License()
                                .name("Uso académico – ECI")
                                .url("https://www.escuelaing.edu.co")))
                .tags(List.of(
                        new Tag().name("Autenticación")
                                .description("Endpoint público de login para obtener un token JWT."),
                        new Tag().name("Blueprints")
                                .description("CRUD de planos (blueprints) protegido por scopes JWT.")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
                .components(new Components().addSecuritySchemes("bearer-jwt",
                        new SecurityScheme()
                                .name("bearer-jwt")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtenido en `/auth/login`. Formato: `Bearer eyJhbGci...`")));
    }
}
