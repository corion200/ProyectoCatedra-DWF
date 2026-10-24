package sv.edu.udb.cfc.catering.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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
import sv.edu.udb.cfc.catering.dto.CateringCreateDTO;
import sv.edu.udb.cfc.catering.dto.CateringResponseDTO;
import sv.edu.udb.cfc.catering.dto.CateringUpdateDTO;
import sv.edu.udb.cfc.catering.enums.EstadoCatering;
import sv.edu.udb.cfc.catering.service.CateringService;

import java.time.LocalDate;

// Controlador de los servicios de catering
@Tag(name = "Catering", description = "Servicios de comida: coffee break, almuerzos y banquetes")
@RestController
@RequestMapping("/api/v1/caterings")
@RequiredArgsConstructor
public class CateringController {

    // Servicio con la lógica de catering
    private final CateringService cateringService;

    // Registra un servicio nuevo
    @Operation(summary = "Registrar un servicio (costoTotal = precio × asistentes)")
    @ApiResponse(responseCode = "201", description = "Servicio registrado, queda PENDIENTE")
    @PostMapping
    public ResponseEntity<CateringResponseDTO> crear(@Valid @RequestBody CateringCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cateringService.crear(dto));
    }

    // Lista los servicios con filtros opcionales
    @Operation(summary = "Listar servicios con filtros y paginación")
    @GetMapping
    public Page<CateringResponseDTO> listar(
            // Filtro por cliente
            @RequestParam(required = false) Long clienteId,
            // Filtro por estado
            @RequestParam(required = false) EstadoCatering estado,
            // Filtro por fecha (yyyy-MM-dd)
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            // Paginación, por defecto 10 por página ordenados por fecha
            @PageableDefault(size = 10, sort = "fechaEvento", direction = Sort.Direction.ASC) Pageable pageable) {
        return cateringService.listar(clienteId, estado, fecha, pageable);
    }

    // Busca un servicio por su id
    @Operation(summary = "Buscar un servicio por id")
    @GetMapping("/{id}")
    public CateringResponseDTO obtenerPorId(@PathVariable Long id) {
        return cateringService.obtenerPorId(id);
    }

    // Actualiza un servicio existente
    @Operation(summary = "Actualizar un servicio (vuelve a calcular el costo total)")
    @PutMapping("/{id}")
    public CateringResponseDTO actualizar(@PathVariable Long id,
                                          @Valid @RequestBody CateringUpdateDTO dto) {
        return cateringService.actualizar(id, dto);
    }

    // Pasa un servicio de PENDIENTE a CONFIRMADO
    @Operation(summary = "Confirmar un servicio PENDIENTE")
    @PatchMapping("/{id}/confirmar")
    public CateringResponseDTO confirmar(@PathVariable Long id) {
        return cateringService.confirmar(id);
    }

    // Marca como finalizado un servicio CONFIRMADO
    @Operation(summary = "Finalizar un servicio CONFIRMADO (ya se entregó)")
    @PatchMapping("/{id}/finalizar")
    public CateringResponseDTO finalizar(@PathVariable Long id) {
        return cateringService.finalizar(id);
    }

    // Cancela un servicio, el motivo es opcional
    @Operation(summary = "Cancelar un servicio activo")
    @PatchMapping("/{id}/cancelar")
    public CateringResponseDTO cancelar(@PathVariable Long id,
                                        @RequestParam(required = false) String motivo) {
        return cateringService.cancelar(id, motivo);
    }

    // Borra un servicio, solo si está PENDIENTE
    @Operation(summary = "Eliminar un servicio (solo los PENDIENTES)")
    @ApiResponse(responseCode = "204", description = "Servicio eliminado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cateringService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}