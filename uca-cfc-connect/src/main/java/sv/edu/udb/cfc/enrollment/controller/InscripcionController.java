package sv.edu.udb.cfc.enrollment.controller;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.cfc.enrollment.dto.InscripcionCreateDTO;
import sv.edu.udb.cfc.enrollment.dto.InscripcionResponseDTO;
import sv.edu.udb.cfc.enrollment.dto.InscripcionUpdateDTO;
import sv.edu.udb.cfc.enrollment.enums.EstadoInscripcion;
import sv.edu.udb.cfc.enrollment.service.InscripcionService;

@Tag(name = "Inscripciones", description = "Inscripción de clientes a cursos y diplomados")
@RestController
@RequestMapping("/api/v1/inscripciones")
@RequiredArgsConstructor
public class InscripcionController {

    private final InscripcionService inscripcionService;

    @Operation(summary = "Registra una inscripción (valida cupo, duplicados y estado del curso)")
    @ApiResponse(responseCode = "201", description = "Inscripción registrada (queda PENDIENTE)")
    @ApiResponse(responseCode = "409", description = "Cupo agotado o inscripción duplicada")
    @PostMapping
    public ResponseEntity<InscripcionResponseDTO> inscribir(@Valid @RequestBody InscripcionCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inscripcionService.inscribir(dto));
    }

    @Operation(summary = "Lista inscripciones con filtros y paginación")
    @GetMapping
    public Page<InscripcionResponseDTO> listar(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long cursoId,
            @RequestParam(required = false) EstadoInscripcion estado,
            @PageableDefault(size = 10, sort = "fechaInscripcion", direction = Sort.Direction.DESC) Pageable pageable) {
        return inscripcionService.listar(clienteId, cursoId, estado, pageable);
    }

    @Operation(summary = "Obtiene una inscripción por id")
    @GetMapping("/{id}")
    public InscripcionResponseDTO obtenerPorId(@PathVariable Long id) {
        return inscripcionService.obtenerPorId(id);
    }

    @Operation(summary = "Actualiza las observaciones de una inscripción")
    @PutMapping("/{id}")
    public InscripcionResponseDTO actualizar(@PathVariable Long id,
                                             @Valid @RequestBody InscripcionUpdateDTO dto) {
        return inscripcionService.actualizarObservaciones(id, dto);
    }

    @Operation(summary = "Confirma una inscripción PENDIENTE")
    @ApiResponse(responseCode = "400", description = "Estado no confirmable")
    @PatchMapping("/{id}/confirmar")
    public InscripcionResponseDTO confirmar(@PathVariable Long id) {
        return inscripcionService.confirmar(id);
    }

    @Operation(summary = "Cancela una inscripción activa (libera el cupo)")
    @PatchMapping("/{id}/cancelar")
    public InscripcionResponseDTO cancelar(@PathVariable Long id,
                                           @RequestParam(required = false) String motivo) {
        return inscripcionService.cancelar(id, motivo);
    }
}