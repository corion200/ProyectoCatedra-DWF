package sv.edu.udb.cfc.quotation.controller;

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
import sv.edu.udb.cfc.quotation.dto.CotizacionCreateDTO;
import sv.edu.udb.cfc.quotation.dto.CotizacionResponseDTO;
import sv.edu.udb.cfc.quotation.dto.CotizacionUpdateDTO;
import sv.edu.udb.cfc.quotation.enums.EstadoCotizacion;
import sv.edu.udb.cfc.quotation.enums.TipoCotizacion;
import sv.edu.udb.cfc.quotation.service.CotizacionService;

@Tag(name = "Cotizaciones", description = "Cotizaciones empresariales: cursos, diplomados, alquileres y catering")
@RestController
@RequestMapping("/api/v1/cotizaciones")
@RequiredArgsConstructor
public class CotizacionController {

    private final CotizacionService cotizacionService;

    @Operation(summary = "Registra una nueva cotización (nace en PENDIENTE)")
    @ApiResponse(responseCode = "201", description = "Cotización creada")
    @PostMapping
    public ResponseEntity<CotizacionResponseDTO> crear(@Valid @RequestBody CotizacionCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cotizacionService.crear(dto));
    }

    @Operation(summary = "Lista cotizaciones con filtros combinables y paginación")
    @GetMapping
    public Page<CotizacionResponseDTO> listar(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) TipoCotizacion tipo,
            @RequestParam(required = false) EstadoCotizacion estado,
            @PageableDefault(size = 10, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable) {
        return cotizacionService.listar(clienteId, tipo, estado, pageable);
    }

    @Operation(summary = "Obtiene una cotización por id")
    @GetMapping("/{id}")
    public CotizacionResponseDTO obtenerPorId(@PathVariable Long id) {
        return cotizacionService.obtenerPorId(id);
    }

    @Operation(summary = "Actualiza una cotización (bloqueado si está APROBADA/RECHAZADA)")
    @PutMapping("/{id}")
    public CotizacionResponseDTO actualizar(@PathVariable Long id,
                                            @Valid @RequestBody CotizacionUpdateDTO dto) {
        return cotizacionService.actualizar(id, dto);
    }

    @Operation(summary = "Cambia el estado (flujo: PENDIENTE → EN_PROCESO → APROBADA/RECHAZADA)")
    @PatchMapping("/{id}/estado/{estado}")
    public CotizacionResponseDTO cambiarEstado(@PathVariable Long id,
                                               @PathVariable EstadoCotizacion estado) {
        return cotizacionService.cambiarEstado(id, estado);
    }

    @Operation(summary = "Elimina una cotización (solo PENDIENTE/RECHAZADA)")
    @ApiResponse(responseCode = "204", description = "Cotización eliminada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cotizacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}