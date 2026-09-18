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

@Tag(name = "Catering", description = "Servicios de alimentación: coffee break, almuerzos, banquetes")
@RestController
@RequestMapping("/api/v1/caterings")
@RequiredArgsConstructor
public class CateringController {

    private final CateringService cateringService;

    @Operation(summary = "Registra un servicio (calcula costoTotal = precio × asistentes)")
    @ApiResponse(responseCode = "201", description = "Servicio registrado (queda PENDIENTE)")
    @PostMapping
    public ResponseEntity<CateringResponseDTO> crear(@Valid @RequestBody CateringCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cateringService.crear(dto));
    }

    @Operation(summary = "Lista servicios con filtros combinables y paginación")
    @GetMapping
    public Page<CateringResponseDTO> listar(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) EstadoCatering estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @PageableDefault(size = 10, sort = "fechaEvento", direction = Sort.Direction.ASC) Pageable pageable) {
        return cateringService.listar(clienteId, estado, fecha, pageable);
    }

    @Operation(summary = "Obtiene un servicio por id")
    @GetMapping("/{id}")
    public CateringResponseDTO obtenerPorId(@PathVariable Long id) {
        return cateringService.obtenerPorId(id);
    }

    @Operation(summary = "Actualiza un servicio (recalcula el costo total)")
    @PutMapping("/{id}")
    public CateringResponseDTO actualizar(@PathVariable Long id,
                                          @Valid @RequestBody CateringUpdateDTO dto) {
        return cateringService.actualizar(id, dto);
    }

    @Operation(summary = "Confirma un servicio PENDIENTE")
    @PatchMapping("/{id}/confirmar")
    public CateringResponseDTO confirmar(@PathVariable Long id) {
        return cateringService.confirmar(id);
    }

    @Operation(summary = "Finaliza un servicio CONFIRMADO (entrega realizada)")
    @PatchMapping("/{id}/finalizar")
    public CateringResponseDTO finalizar(@PathVariable Long id) {
        return cateringService.finalizar(id);
    }

    @Operation(summary = "Cancela un servicio activo")
    @PatchMapping("/{id}/cancelar")
    public CateringResponseDTO cancelar(@PathVariable Long id,
                                        @RequestParam(required = false) String motivo) {
        return cateringService.cancelar(id, motivo);
    }

    @Operation(summary = "Elimina un servicio (solo PENDIENTES)")
    @ApiResponse(responseCode = "204", description = "Servicio eliminado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cateringService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}