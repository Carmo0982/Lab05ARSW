package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.controllers.response.ApiResponse;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Tag(name = "Blueprints", description = "CRUD de planos (blueprints) por autor y nombre")
@RestController
@RequestMapping("/api/v1/blueprints")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) { this.services = services; }

    // GET /blueprints -> 200 ok
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    @Operation(summary = "Obtener todos los planos", description = "Retorna el conjunto completo de blueprints registrados.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de blueprints obtenida correctamente")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {
        ApiResponse<Set<Blueprint>> apiResponse = new ApiResponse<>(200, "execute ok", services.getAllBlueprints());
        return ResponseEntity.ok(apiResponse);
    }

    // GET /blueprints/{author} -> 200 OK o 404 Not Found
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    @Operation(summary = "Obtener planos por autor", description = "Retorna todos los blueprints que pertenecen al autor indicado.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blueprints del autor encontrados"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Autor no encontrado")
    })
    @GetMapping("/{author}")
    public ResponseEntity<ApiResponse<Set<Blueprint>>> byAuthor(
            @Parameter(description = "Nombre del autor") @PathVariable String author) {
        try {
            Set<Blueprint> result = services.getBlueprintsByAuthor(author);
            ApiResponse<Set<Blueprint>> apiResponse = new ApiResponse<>(200, "OK", result);
            return ResponseEntity.ok(apiResponse);
        } catch (BlueprintNotFoundException e) {
            ApiResponse<Set<Blueprint>> apiResponse = new ApiResponse<>(404, "Not Found", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        }
    }

    // GET /blueprints/{author}/{bpname} -> 200 OK o 404 Not Found
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    @Operation(summary = "Obtener un plano por autor y nombre", description = "Retorna el blueprint específico identificado por autor y nombre.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blueprint encontrado"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint no encontrado")
    })
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<ApiResponse<Blueprint>> byAuthorAndName(
            @Parameter(description = "Nombre del autor") @PathVariable String author,
            @Parameter(description = "Nombre del blueprint") @PathVariable String bpname) {
        try {
            Blueprint op = services.getBlueprint(author, bpname);
            ApiResponse<Blueprint> apiResponse = new ApiResponse<>(200, "OK", op);
            return ResponseEntity.ok(apiResponse);
        } catch (BlueprintNotFoundException e) {
            ApiResponse<Blueprint> apiResponse = new ApiResponse<>(404, "Not Found", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        }
    }

    // POST /blueprints -> 201 Created o 409 Conflict
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    @Operation(summary = "Crear un nuevo plano", description = "Registra un blueprint nuevo. Retorna 409 si ya existe.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Blueprint creado correctamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "El blueprint ya existe")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Blueprint>> add(@Valid @RequestBody NewBlueprintRequest req) {
        try {
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            ApiResponse<Blueprint> apiResponse = new ApiResponse<>(201, "CREATED", bp);
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
        } catch (BlueprintPersistenceException e) {
            ApiResponse<Blueprint> apiResponse = new ApiResponse<>(409, "CONFLICT", null);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(apiResponse);
        }
    }

    // PUT /blueprints/{author}/{bpname}/points
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    @Operation(summary = "Agregar un punto a un plano", description = "Añade un nuevo punto (x, y) al blueprint indicado.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Punto agregado correctamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint no encontrado")
    })
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<ApiResponse<Blueprint>> addPoint(
            @Parameter(description = "Nombre del autor") @PathVariable String author,
            @Parameter(description = "Nombre del blueprint") @PathVariable String bpname,
            @RequestBody Point p) {
        try {
            services.addPoint(author, bpname, p.getX(), p.getY());
            Blueprint bp = services.getBlueprint(author, bpname);
            ApiResponse<Blueprint> apiResponse = new ApiResponse<>(202, "ACEPTED", bp);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(apiResponse);
        } catch (BlueprintNotFoundException e) {
            ApiResponse<Blueprint> apiResponse = new ApiResponse<>(404, "Not Found", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        }
    }

    public record NewBlueprintRequest(
            @NotBlank String author,
            @NotBlank String name,
            @Valid List<Point> points
    ) { }

}

