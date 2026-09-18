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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.cfc.academic.dto.CursoCreateDTO;
import sv.edu.udb.cfc.academic.dto.CursoResponseDTO;
import sv.edu.udb.cfc.academic.dto.CursoUpdateDTO;
import sv.edu.udb.cfc.academic.enums.EstadoCurso;
import sv.edu.udb.cfc.academic.enums.TipoOferta;
import sv.edu.udb.cfc.academic.service.CursoService;

@Tag(name = "Cursos y Diplomados", description = "Oferta académica del CFC (cursos cortos y diplomados)")
@RestController
@RequestMapping("/api/v1/cursos")
@RequiredArgsConstructor
public class CursoController {

    private final CursoService cursoService;

    @Operation(summary = "Registra un nuevo curso o diplomado")
    @ApiResponse(responseCode = "201", description = "Curso creado")
    @ApiResponse(responseCode = "409", description = "Código duplicado o docente inactivo")
    @PostMapping
    public ResponseEntity<CursoResponseDTO> crear(@Valid @RequestBody CursoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cursoService.crear(dto));
    }

    @Operation(summary = "Lista cursos con filtros combinables, paginación y ordenamiento")
    @GetMapping
    public Page<CursoResponseDTO> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) TipoOferta tipo,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long modalidadId,
            @RequestParam(required = false) EstadoCurso estado,
            @PageableDefault(size = 10, sort = "fechaInicio", direction = Sort.Direction.ASC) Pageable pageable) {
        return cursoService.listar(nombre, tipo, categoriaId, modalidadId, estado, pageable);
    }

    @Operation(summary = "Obtiene un curso por id")
    @GetMapping("/{id}")
    public CursoResponseDTO obtenerPorId(@PathVariable Long id) {
        return cursoService.obtenerPorId(id);
    }

    @Operation(summary = "Actualiza un curso existente (incluye reasignación de docentes)")
    @PutMapping("/{id}")
    public CursoResponseDTO actualizar(@PathVariable Long id,
                                       @Valid @RequestBody CursoUpdateDTO dto) {
        return cursoService.actualizar(id, dto);
    }

    @Operation(summary = "Cambia el estado del curso (máquina de estados: PROGRAMADO → EN_CURSO → FINALIZADO/CANCELADO)")
    @ApiResponse(responseCode = "400", description = "Transición de estado no permitida")
    @PatchMapping("/{id}/estado/{estado}")
    public CursoResponseDTO cambiarEstado(@PathVariable Long id, @PathVariable EstadoCurso estado) {
        return cursoService.cambiarEstado(id, estado);
    }

    @Operation(summary = "Desactiva un curso (soft delete: conserva historial de inscripciones)")
    @ApiResponse(responseCode = "204", description = "Curso desactivado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cursoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}