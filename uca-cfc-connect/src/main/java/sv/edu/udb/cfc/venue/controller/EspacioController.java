package sv.edu.udb.cfc.venue.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.cfc.venue.dto.EspacioCreateDTO;
import sv.edu.udb.cfc.venue.dto.EspacioResponseDTO;
import sv.edu.udb.cfc.venue.dto.EspacioUpdateDTO;
import sv.edu.udb.cfc.venue.enums.TipoEspacio;
import sv.edu.udb.cfc.venue.service.EspacioService;

import java.util.List;

@Tag(name = "Espacios", description = "Auditorios, salas y laboratorios disponibles para alquiler")
@RestController
@RequestMapping("/api/v1/espacios")
@RequiredArgsConstructor
public class EspacioController {

    private final EspacioService espacioService;

    @Operation(summary = "Registra un nuevo espacio")
    @ApiResponse(responseCode = "201", description = "Espacio creado")
    @ApiResponse(responseCode = "409", description = "Código duplicado")
    @PostMapping
    public ResponseEntity<EspacioResponseDTO> crear(@Valid @RequestBody EspacioCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(espacioService.crear(dto));
    }

    @Operation(summary = "Lista espacios con filtros combinables (nombre, tipo, capacidad mínima, disponibilidad)")
    @GetMapping
    public Page<EspacioResponseDTO> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) TipoEspacio tipo,
            @RequestParam(required = false) Integer capacidadMinima,
            @RequestParam(required = false) Boolean disponible,
            @PageableDefault(size = 10, sort = "capacidad", direction = Sort.Direction.DESC) Pageable pageable) {
        return espacioService.listar(nombre, tipo, capacidadMinima, disponible, pageable);
    }

    @Operation(summary = "Catálogo público: espacios activos y disponibles")
    @GetMapping("/disponibles")
    public List<EspacioResponseDTO> listarDisponibles() {
        return espacioService.listarDisponibles();
    }

    @Operation(summary = "Obtiene un espacio por id")
    @GetMapping("/{id}")
    public EspacioResponseDTO obtenerPorId(@PathVariable Long id) {
        return espacioService.obtenerPorId(id);
    }

    @Operation(summary = "Actualiza un espacio existente")
    @PutMapping("/{id}")
    public EspacioResponseDTO actualizar(@PathVariable Long id,
                                         @Valid @RequestBody EspacioUpdateDTO dto) {
        return espacioService.actualizar(id, dto);
    }

    @Operation(summary = "Desactiva un espacio (soft delete)")
    @ApiResponse(responseCode = "204", description = "Espacio desactivado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        espacioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}