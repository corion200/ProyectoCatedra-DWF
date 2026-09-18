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
import sv.edu.udb.cfc.venue.dto.AlquilerCreateDTO;
import sv.edu.udb.cfc.venue.dto.AlquilerResponseDTO;
import sv.edu.udb.cfc.venue.dto.AlquilerUpdateDTO;
import sv.edu.udb.cfc.venue.enums.EstadoAlquiler;
import sv.edu.udb.cfc.venue.service.AlquilerService;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Alquileres", description = "Reservas de espacios con detección de solapamiento de horarios")
@RestController
@RequestMapping("/api/v1/alquileres")
@RequiredArgsConstructor
public class AlquilerController {

    private final AlquilerService alquilerService;

    @Operation(summary = "Registra una reserva (calcula el costo y rechaza horarios que se crucen)")
    @ApiResponse(responseCode = "201", description = "Reserva registrada (queda PENDIENTE)")
    @ApiResponse(responseCode = "409", description = "El espacio ya está reservado en ese horario")
    @PostMapping
    public ResponseEntity<AlquilerResponseDTO> crear(@Valid @RequestBody AlquilerCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alquilerService.crear(dto));
    }

    @Operation(summary = "Lista reservas con filtros combinables y paginación")
    @GetMapping
    public Page<AlquilerResponseDTO> listar(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long espacioId,
            @RequestParam(required = false) EstadoAlquiler estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @PageableDefault(size = 10, sort = "fechaEvento", direction = Sort.Direction.DESC) Pageable pageable) {
        return alquilerService.listar(clienteId, espacioId, estado, fecha, pageable);
    }

    @Operation(summary = "Horarios OCUPADOS de un espacio en una fecha (para mostrar agenda)")
    @GetMapping("/ocupacion")
    public List<AlquilerResponseDTO> ocupacion(
            @RequestParam Long espacioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return alquilerService.ocupacionDeEspacio(espacioId, fecha);
    }

    @Operation(summary = "Obtiene una reserva por id")
    @GetMapping("/{id}")
    public AlquilerResponseDTO obtenerPorId(@PathVariable Long id) {
        return alquilerService.obtenerPorId(id);
    }

    @Operation(summary = "Reprograma una reserva (re-valida solapamiento y recalcula el costo)")
    @PutMapping("/{id}")
    public AlquilerResponseDTO actualizar(@PathVariable Long id,
                                          @Valid @RequestBody AlquilerUpdateDTO dto) {
        return alquilerService.actualizar(id, dto);
    }

    @Operation(summary = "Confirma una reserva PENDIENTE")
    @PatchMapping("/{id}/confirmar")
    public AlquilerResponseDTO confirmar(@PathVariable Long id) {
        return alquilerService.confirmar(id);
    }

    @Operation(summary = "Finaliza una reserva CONFIRMADA (evento realizado)")
    @PatchMapping("/{id}/finalizar")
    public AlquilerResponseDTO finalizar(@PathVariable Long id) {
        return alquilerService.finalizar(id);
    }

    @Operation(summary = "Cancela una reserva activa (libera el horario)")
    @PatchMapping("/{id}/cancelar")
    public AlquilerResponseDTO cancelar(@PathVariable Long id,
                                        @RequestParam(required = false) String motivo) {
        return alquilerService.cancelar(id, motivo);
    }

    @Operation(summary = "Elimina una reserva (solo PENDIENTES; las demás se cancelan)")
    @ApiResponse(responseCode = "204", description = "Reserva eliminada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        alquilerService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}