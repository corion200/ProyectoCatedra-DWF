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
import sv.edu.udb.cfc.academic.dto.CategoriaCreateDTO;
import sv.edu.udb.cfc.academic.dto.CategoriaResponseDTO;
import sv.edu.udb.cfc.academic.dto.CategoriaUpdateDTO;
import sv.edu.udb.cfc.academic.service.CategoriaService;

@Tag(name = "Categorías", description = "Catálogo de categorías académicas del CFC")
@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @Operation(summary = "Registra una nueva categoría")
    @ApiResponse(responseCode = "201", description = "Categoría creada")
    @ApiResponse(responseCode = "409", description = "Nombre duplicado")
    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> crear(@Valid @RequestBody CategoriaCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.crear(dto));
    }

    @Operation(summary = "Lista categorías con filtros y paginación")
    @GetMapping
    public Page<CategoriaResponseDTO> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(size = 10, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {
        return categoriaService.listar(nombre, activo, pageable);
    }

    @Operation(summary = "Obtiene una categoría por id")
    @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    @GetMapping("/{id}")
    public CategoriaResponseDTO obtenerPorId(@PathVariable Long id) {
        return categoriaService.obtenerPorId(id);
    }

    @Operation(summary = "Actualiza una categoría existente")
    @PutMapping("/{id}")
    public CategoriaResponseDTO actualizar(@PathVariable Long id,
                                           @Valid @RequestBody CategoriaUpdateDTO dto) {
        return categoriaService.actualizar(id, dto);
    }

    @Operation(summary = "Elimina una categoría (rechazada si tiene cursos asociados)")
    @ApiResponse(responseCode = "204", description = "Categoría eliminada/desactivada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}