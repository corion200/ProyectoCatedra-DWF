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
import sv.edu.udb.cfc.academic.dto.DocenteCreateDTO;
import sv.edu.udb.cfc.academic.dto.DocenteResponseDTO;
import sv.edu.udb.cfc.academic.dto.DocenteUpdateDTO;
import sv.edu.udb.cfc.academic.service.DocenteService;

@Tag(name = "Docentes", description = "Administración de los docentes del CFC")
@RestController
@RequestMapping("/api/v1/docentes")
@RequiredArgsConstructor
public class DocenteController {

    private final DocenteService docenteService;

    @Operation(summary = "Crea un nuevo docente")
    @ApiResponse(responseCode = "201", description = "Docente creado correctamente")
    @ApiResponse(responseCode = "409", description = "El DUI o el correo ya están registrados")
    @PostMapping
    public ResponseEntity<DocenteResponseDTO> crear(@Valid @RequestBody DocenteCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(docenteService.crear(dto));
    }

    @Operation(summary = "Consulta docentes con paginación (el parámetro 'busqueda' filtra por nombre o apellido)")
    @GetMapping
    public Page<DocenteResponseDTO> listar(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(size = 10, sort = "apellidos", direction = Sort.Direction.ASC) Pageable pageable) {
        return docenteService.listar(busqueda, activo, pageable);
    }

    @Operation(summary = "Busca un docente por su identificador")
    @GetMapping("/{id}")
    public DocenteResponseDTO obtenerPorId(@PathVariable Long id) {
        return docenteService.obtenerPorId(id);
    }

    @Operation(summary = "Modifica los datos de un docente existente")
    @PutMapping("/{id}")
    public DocenteResponseDTO actualizar(@PathVariable Long id,
                                         @Valid @RequestBody DocenteUpdateDTO dto) {
        return docenteService.actualizar(id, dto);
    }

    @Operation(summary = "Desactiva un docente (borrado lógico)")
    @ApiResponse(responseCode = "204", description = "Docente desactivado correctamente")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        docenteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}