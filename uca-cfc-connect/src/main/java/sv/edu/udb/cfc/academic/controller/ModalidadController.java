package sv.edu.udb.cfc.academic.controller;

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
import sv.edu.udb.cfc.academic.dto.ModalidadCreateDTO;
import sv.edu.udb.cfc.academic.dto.ModalidadResponseDTO;
import sv.edu.udb.cfc.academic.dto.ModalidadUpdateDTO;
import sv.edu.udb.cfc.academic.service.ModalidadService;

@Tag(name = "Modalidades", description = "Modalidades de impartición: Presencial, Virtual, Híbrida")
@RestController
@RequestMapping("/api/v1/modalidades")
@RequiredArgsConstructor
public class ModalidadController {

    private final ModalidadService modalidadService;

    @Operation(summary = "Registra una nueva modalidad")
    @ApiResponse(responseCode = "201", description = "Modalidad creada")
    @PostMapping
    public ResponseEntity<ModalidadResponseDTO> crear(@Valid @RequestBody ModalidadCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(modalidadService.crear(dto));
    }

    @Operation(summary = "Lista modalidades (filtro opcional: solo activas)")
    @GetMapping
    public Page<ModalidadResponseDTO> listar(
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(size = 10, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {
        return modalidadService.listar(activo, pageable);
    }

    @Operation(summary = "Obtiene una modalidad por id")
    @GetMapping("/{id}")
    public ModalidadResponseDTO obtenerPorId(@PathVariable Long id) {
        return modalidadService.obtenerPorId(id);
    }

    @Operation(summary = "Actualiza una modalidad existente")
    @PutMapping("/{id}")
    public ModalidadResponseDTO actualizar(@PathVariable Long id,
                                           @Valid @RequestBody ModalidadUpdateDTO dto) {
        return modalidadService.actualizar(id, dto);
    }

    @Operation(summary = "Elimina una modalidad (rechazada si tiene cursos asociados)")
    @ApiResponse(responseCode = "204", description = "Modalidad eliminada/desactivada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        modalidadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}