package sv.edu.udb.cfc.payment.controller;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.cfc.payment.dto.PagoCreateDTO;
import sv.edu.udb.cfc.payment.dto.PagoResponseDTO;
import sv.edu.udb.cfc.payment.dto.ResumenPagoDTO;
import sv.edu.udb.cfc.payment.enums.EstadoPago;
import sv.edu.udb.cfc.payment.enums.MetodoPago;
import sv.edu.udb.cfc.payment.service.PagoService;

@Tag(name = "Pagos", description = "Pagos de inscripciones, cotizaciones, alquileres y catering")
@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @Operation(summary = "Registra un pago asociado a EXACTAMENTE un documento (inscripción/cotización/alquiler/catering)")
    @ApiResponse(responseCode = "201", description = "Pago registrado (queda PENDIENTE)")
    @ApiResponse(responseCode = "400", description = "Cero o múltiples documentos referenciados")
    @PostMapping
    public ResponseEntity<PagoResponseDTO> registrar(@Valid @RequestBody PagoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.registrar(dto));
    }

    @Operation(summary = "Lista pagos con filtros combinables y paginación")
    @GetMapping
    public Page<PagoResponseDTO> listar(
            @RequestParam(required = false) EstadoPago estado,
            @RequestParam(required = false) MetodoPago metodoPago,
            @RequestParam(required = false) Long inscripcionId,
            @PageableDefault(size = 10, sort = "fechaPago", direction = Sort.Direction.DESC) Pageable pageable) {
        return pagoService.listar(estado, metodoPago, inscripcionId, pageable);
    }

    @Operation(summary = "Estado de cuenta de una inscripción: precio del curso, total pagado y saldo pendiente")
    @GetMapping("/resumen/inscripcion/{inscripcionId}")
    public ResumenPagoDTO resumenInscripcion(@PathVariable Long inscripcionId) {
        return pagoService.resumenInscripcion(inscripcionId);
    }

    @Operation(summary = "Obtiene un pago por id")
    @GetMapping("/{id}")
    public PagoResponseDTO obtenerPorId(@PathVariable Long id) {
        return pagoService.obtenerPorId(id);
    }

    @Operation(summary = "Actualiza el estado (flujo: PENDIENTE → PARCIAL → PAGADO; sin retorno)")
    @ApiResponse(responseCode = "400", description = "Transición de estado no permitida")
    @PatchMapping("/{id}/estado/{estado}")
    public PagoResponseDTO cambiarEstado(@PathVariable Long id, @PathVariable EstadoPago estado) {
        return pagoService.cambiarEstado(id, estado);
    }

    @Operation(summary = "Elimina un pago (bloqueado si está PAGADO)")
    @ApiResponse(responseCode = "204", description = "Pago eliminado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}