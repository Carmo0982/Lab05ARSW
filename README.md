# Escuela Colombiana de Ingeniería Julio Garavito
## Arquitectura de Software – ARSW
### Laboratorio – Parte 2: BluePrints API con Seguridad JWT (OAuth 2.0)

Este laboratorio extiende la **Parte 1** ([Lab_P1_BluePrints_Java21_API](https://github.com/DECSIS-ECI/Lab_P1_BluePrints_Java21_API)) agregando **seguridad a la API** usando **Spring Boot 3, Java 21 y JWT (OAuth 2.0)**.  
El API se convierte en un **Resource Server** protegido por tokens Bearer firmados con **RS256**.  
Incluye un endpoint didáctico `/auth/login` que emite el token para facilitar las pruebas.

---

## Objetivos
- Implementar seguridad en servicios REST usando **OAuth2 Resource Server**.
- Configurar emisión y validación de **JWT**.
- Proteger endpoints con **roles y scopes** (`blueprints.read`, `blueprints.write`).
- Integrar la documentación de seguridad en **Swagger/OpenAPI**.

---

## Requisitos
- JDK 21
- Maven 3.9+
- Git

---

## Ejecución del proyecto
1. Clonar o descomprimir el proyecto:
   ```bash
   git clone https://github.com/DECSIS-ECI/Lab_P2_BluePrints_Java21_API_Security_JWT.git
   cd Lab_P2_BluePrints_Java21_API_Security_JWT
   ```
   ó si el profesor entrega el `.zip`, descomprimirlo y entrar en la carpeta.

2. Ejecutar con Maven:
   ```bash
   mvn -q -DskipTests spring-boot:run
   ```

3. Verificar que la aplicación levante en `http://localhost:8080`.

---

## Endpoints principales

### 1. Login (emite token)
```
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "student",
  "password": "student123"
}
```
Respuesta:
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

### 2. Consultar blueprints (requiere scope `blueprints.read`)
```
GET http://localhost:8080/api/blueprints
Authorization: Bearer <ACCESS_TOKEN>
```

### 3. Crear blueprint (requiere scope `blueprints.write`)
```
POST http://localhost:8080/api/blueprints
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json

{
  "name": "Nuevo Plano"
}
```

---

## Swagger UI
- URL: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- Pulsa **Authorize**, ingresa el token en el formato:
  ```
  Bearer eyJhbGciOi...
  ```

---

## Estructura del proyecto
```
src/main/java/co/edu/eci/blueprints/
  ├── api/BlueprintController.java       # Endpoints protegidos
  ├── auth/AuthController.java           # Login didáctico para emitir tokens
  ├── config/OpenApiConfig.java          # Configuración Swagger + JWT
  └── security/
       ├── SecurityConfig.java
       ├── MethodSecurityConfig.java
       ├── JwtKeyProvider.java
       ├── InMemoryUserService.java
       └── RsaKeyProperties.java
src/main/resources/
  └── application.yml
```

---

## Actividades propuestas
1. Revisar el código de configuración de seguridad (`SecurityConfig`) e identificar cómo se definen los endpoints públicos y protegidos.
2. Explorar el flujo de login y analizar las claims del JWT emitido.
3. Extender los scopes (`blueprints.read`, `blueprints.write`) para controlar otros endpoints de la API, del laboratorio P1 trabajado.
4. Modificar el tiempo de expiración del token y observar el efecto.
5. Documentar en Swagger los endpoints de autenticación y de negocio.

---

## Lecturas recomendadas
- [Spring Security Reference – OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [Spring Boot – Securing Web Applications](https://spring.io/guides/gs/securing-web/)
- [JSON Web Tokens – jwt.io](https://jwt.io/introduction)

---

## Licencia
Proyecto educativo con fines académicos – Escuela Colombiana de Ingeniería Julio Garavito.


---
# Informe de laboratorio

**Autores**:

- *Jacobo Diaz Alvarado*

- *Santiango Carmona Pineda*

## Entendiendo `security`

### Clase `InMemoryUserService`

- Esta clase tiene dos atributos: `users` que es un *Map* donde ambas claves son *String*. También está `encoder` que es de tipo
  *PasswordEncoder*. Leyendo la documentación encontramos que este es una interfaz que permite cifrar contraseñas de forma segura.
- **Constructor**: el constructor recibe solo la interfaz de *PasswordEncoder* y añade al *Map* un *student* y su contraseña y antes de añadir la contraseña la cifra. Lo mismo hace para el usuario *assistant*.
- **isValid(String username, String rawPassword)**: este método se encarga de verificar si dado un usuario y una contraseña, busca el usuario y verifica que la contraseña registrada en el *Map* coincida con la contraseña dada.

### Clase `JwtKeyProvider`
- `@Component`: registra la clase como un bean de Spring, lo que permite inyectarla en otros componentes con `@Autowired`.
- Esta clase tiene un único atributo keyPair de tipo KeyPair. Investigando en la documentación nos damos cuenta de que se usa para encriptar y desencriptar. Además cuenta con una clave pública y privada.
- Tiene los respectivos getters para obtener la clave pública y privada.
- **@PostConstruct void init()**: Este método se ejecuta automáticamente una sola vez justo después de que Spring crea el bean. Aquí es donde se generan las claves.
- Algoritmo RSA: Se basa en un problema matemático muy difícil de resolver: factorizar números muy grandes.


Por el momento esta clase se encuentra vacía.

### Record `RsaKeyProperties`
Esta clase mapea propiedades del archivo de configuración.
- Consta de dos atributos: `issuer` de tipo *String*; este identifica quién emitió el JWT y `tokenTtlSeconds` de tipo *Integer* es el tiempo de vida de ese token.

### Clase `SecurityConfig`

- Define qué URLs son públicas (login, swagger, health) y cuáles requieren token (/api/**).
- Configura que los tokens JWT se validen automáticamente en cada request (oauth2ResourceServer).
- Registra el JwtDecoder para verificar tokens entrantes con la clave pública RSA.
- Registra el JwtEncoder para crear y firmar tokens con la clave privada RSA.

---
## Analizando Auth
### Clase `AuthController`

- **Atributos**: 
    1. `encoder`: es de tipo *JwtEncoder*.
    2. `userService`: es de tipo *InMemoryUserService*.
    3. `props`: es de tipo  *RsaKeyProperties*.
- **Constructor**: inicializada cada uno de los atributos.
- **LoginRequest**: es un record que representa el cuerpo del request.
```json
{
  "username": "john.doe",
  "password": "1234"
}
```
- **TokenResponse**: Es un record que representa la respuesta que se le devuelve al cliente tras un login exitoso
```json
{
  "access_token": "eyJhbGci...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```
- `login(@RequestBody LoginRequest req)`: 
  1. Valida credenciales.
  2. Calcula tiempo de expiración a partir del atributo `props`.
  3. `String scope = "blueprints.read blueprints.write";` define los permisos del token.
  4. **JwtClaims**
    ```java
                JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(props.issuer())//Quién emitió el token
            .issuedAt(now)//Cuando se emitió el token
            .expiresAt(exp)//Cuando expira el token
            .subject(req.username())//A quién pertenece
            .claim("scope", scope)//Que permisos tiene
            .build();
    ```
  5. Firmar con clave privada RSA
  6. Devolver token al cliente

---
# Juntando con la parte I

### Dependencias necesarias

Para que todo funcione, el `pom.xml` debe tener estas dependencias además de las del lab anterior:

```xml

    org.springframework.boot
    spring-boot-starter-security



    org.springframework.boot
    spring-boot-starter-oauth2-resource-server

```

### Configuración en `application.yml`

```yaml
server:
  port: 8080

spring:
  main:
    allow-bean-definition-overriding: true

blueprints:
  security:
    issuer: "https://decsis-eci/blueprints"
    token-ttl-seconds: 3600
```

> ⚠️ No agregar `jwk-set-uri` ya que las claves se generan localmente con `JwtKeyProvider`.

### Configuración de Swagger

En `application.properties` se cambió la ruta por defecto de la documentación:

```properties
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

Por eso en `SecurityConfig` se debe permitir `/api-docs/**` y no `/v3/api-docs/**`:

```java
.requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
```

### Extendiendo los scopes al `BlueprintsAPIController`

Se agregó `@EnableMethodSecurity` en `SecurityConfig` para habilitar `@PreAuthorize`:

```java
@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(RsaKeyProperties.class)
public class SecurityConfig { ... }
```

Luego se aplicó `@PreAuthorize` en cada método del controller según si es de lectura o escritura:

```java
// Lectura
@PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
@GetMapping
public ResponseEntity<ApiResponse<Set>> getAll() { ... }

// Escritura
@PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
@PostMapping
public ResponseEntity<ApiResponse> add(...) { ... }
```



### Modificar el tiempo de expiración del token

El tiempo de vida (TTL) del JWT se configura en `application.yml` mediante la propiedad `token-ttl-seconds`:

```yaml
blueprints:
  security:
    issuer: "https://decsis-eci/blueprints"
    token-ttl-seconds: 30  
```

Esta propiedad es leída por el record `RsaKeyProperties`:

```java
@ConfigurationProperties(prefix = "blueprints.security")
public record RsaKeyProperties(String issuer, Integer tokenTtlSeconds) {}
```

Y utilizada en `AuthController` al momento de construir el token:

```java
long ttl = props.tokenTtlSeconds() != null ? props.tokenTtlSeconds() : 3600;
Instant exp = now.plusSeconds(ttl);
```

#### Efecto observado

1. **Con `token-ttl-seconds: 3600`** (valor original): el token es válido durante 1 hora. Todas las peticiones autenticadas funcionan con normalidad durante ese periodo.

2. **Con `token-ttl-seconds: 30`** (valor modificado): el token expira a los 30 segundos. Si se realiza un login y luego se espera más de 30 segundos antes de hacer una petición protegida, el servidor responde con **401 Unauthorized** y el siguiente error:

   ```
   401 Unauthorized — "An error occurred while attempting to decode the Jwt: Jwt expired"
   ```

   Esto sucede porque Spring Security valida automáticamente el claim `exp` del JWT. Si `Instant.now()` es posterior a `exp`, el token se rechaza.

3. **Respuesta del login**: el campo `expires_in` de la respuesta refleja el TTL configurado:
   ```json
   {
     "access_token": "eyJhbGciOiJSUzI1NiJ9...",
     "token_type": "Bearer",
     "expires_in": 30
   }
   ```

**Conclusión**: reducir el TTL mejora la seguridad (menor ventana de uso si el token es robado), pero obliga al cliente a re-autenticarse con más frecuencia. En producción, un valor común es entre 300 (5 min) y 3600 (1 hora), complementado con *refresh tokens*.

### Probar sin seguridad

Para deshabilitar la seguridad temporalmente, en `SecurityConfig` cambiar:

```java
.authorizeHttpRequests(auth -> auth
    .anyRequest().permitAll()
)
```

### 5. Documentar en Swagger los endpoints de autenticación y de negocio

Se documentaron **todos** los endpoints (autenticación y negocio) usando anotaciones de **Swagger / OpenAPI 3** para que la interfaz de Swagger UI sea autocontenida y permita probar la API sin herramientas externas.

#### Cambios en `OpenApiConfig`

Se mejoró la configuración global de OpenAPI para incluir:

- **Descripción enriquecida** con Markdown: flujo de autenticación paso a paso y tabla de usuarios de prueba.
- **Tags organizados**: `Autenticación` y `Blueprints`, para agrupar visualmente los endpoints.
- **Información de contacto y licencia**.
- **Descripción del esquema de seguridad** `bearer-jwt` para guiar al usuario en Swagger UI.

```java
.info(new Info()
        .title("BluePrints API")
        .version("2.0")
        .description("API REST para gestión de planos protegida con JWT (OAuth 2.0).\n\n"
                + "## Flujo de autenticación\n"
                + "1. POST /auth/login con credenciales válidas.\n"
                + "2. Copiar el access_token.\n"
                + "3. Pulsar Authorize e ingresar: Bearer <access_token>.\n"
                + "4. Los endpoints protegidos enviarán el token automáticamente.")
        .contact(new Contact().name("Jacobo Diaz & Santiago Carmona"))
        .license(new License().name("Uso académico – ECI")))
.tags(List.of(
        new Tag().name("Autenticación").description("Endpoint público de login para obtener un token JWT."),
        new Tag().name("Blueprints").description("CRUD de planos protegido por scopes JWT.")))
```

#### Cambios en `AuthController`

Se agregaron las siguientes anotaciones:

| Anotación | Propósito |
|-----------|-----------|
| `@Tag(name = "Autenticación")` | Agrupa el endpoint bajo la sección "Autenticación" en Swagger |
| `@Operation(summary, description)` | Documenta el propósito del login y el flujo de uso del token |
| `@ApiResponses` | Documenta las respuestas `200` (token emitido) y `401` (credenciales inválidas) con ejemplos JSON |
| `@SecurityRequirements` (vacío) | Indica que `/auth/login` **no requiere token**, removiendo el candado 🔒 |
| `@Schema` en `LoginRequest` | Documenta los campos `username` y `password` con ejemplos (`student`, `student123`) |
| `@Schema` en `TokenResponse` | Documenta los campos `access_token`, `token_type` y `expires_in` |
| `@ExampleObject` | Muestra ejemplos concretos de request y response en Swagger UI |

Ejemplo de la anotación en el método `login`:

```java
@Operation(
    summary = "Iniciar sesión",
    description = "Autentica al usuario y retorna un token JWT firmado con RS256..."
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Login exitoso — token JWT emitido",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = TokenResponse.class),
            examples = @ExampleObject(value = "{\"access_token\":\"eyJ...\",\"token_type\":\"Bearer\",\"expires_in\":3600}"))),
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas",
        content = @Content(examples = @ExampleObject(value = "{\"error\":\"invalid_credentials\"}")))
})
@SecurityRequirements  // No requiere token
@PostMapping("/login")
```

#### Cambios en `BlueprintsAPIController`

Se enriquecieron las anotaciones existentes de cada endpoint:

1. **`@SecurityRequirement(name = "bearer-jwt")`** en cada `@Operation`: Swagger UI muestra el candado y envía el token automáticamente.
2. **Respuestas `401` y `403`**: se agregaron a todos los endpoints protegidos para documentar los errores de autenticación y autorización.
3. **Scope requerido en la descripción**: cada endpoint indica explícitamente si necesita `blueprints.read` o `blueprints.write`.

Ejemplo:

```java
@Operation(summary = "Obtener todos los planos",
    description = "Retorna el conjunto completo de blueprints. Requiere scope blueprints.read.",
    security = @SecurityRequirement(name = "bearer-jwt"))
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Lista de blueprints obtenida correctamente"),
    @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
    @ApiResponse(responseCode = "403", description = "Token sin el scope requerido (blueprints.read)")
})
```

#### Resultado en Swagger UI

Al acceder a `http://localhost:8080/swagger-ui/index.html` se observa:

1. **Descripción general** con flujo de autenticación y usuarios de prueba.
2. **Dos secciones** claramente separadas: *Autenticación* y *Blueprints*.
3. El endpoint `POST /auth/login` aparece **sin candado** (público).
4. Los endpoints de `/api/v1/blueprints/**` aparecen **con candado** .
5. Al pulsar **Authorize** e ingresar el token, se puede probar toda la API directamente desde el navegador.
6. Cada endpoint muestra sus posibles respuestas: `200`, `201`, `401`, `403`, `404`, `409` según corresponda.

