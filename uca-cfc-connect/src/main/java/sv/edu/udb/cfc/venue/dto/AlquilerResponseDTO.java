package sv.edu.udb.cfc.venue.dto;

import sv.edu.udb.cfc.venue.entity.Alquiler;
import sv.edu.udb.cfc.venue.enums.EstadoAlquiler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Datos para registrar una reserva. El costo no viene del cliente: lo calcula
 * el servicio. Este DTO solo valida formato y obligatoriedad; el solapamiento
 * de horarios se verifica en {@link AlquilerService} al crear.
 */
public record AlquilerResponseDTO(
        Long id, String codigo, Long espacioId, String espacioNombre,
        Long clienteId, String clienteNombre, Long cotizacionId,
        LocalDate fechaEvento, LocalTime horaInicio, LocalTime horaFin,
        BigDecimal costoTotal, EstadoAlquiler estado, String observaciones) {

    /**
     * Respuesta de una reserva. Incluye los nombres del espacio y del cliente
     * ya resueltos para que el front no tenga que consultarlos aparte.
     */
    public static AlquilerResponseDTO from(Alquiler a) {
        return new AlquilerResponseDTO(a.getId(), a.getCodigo(),
                a.getEspacio().getId(), a.getEspacio().getNombre(),
                a.getCliente().getId(), a.getCliente().getNombre(),
                a.getCotizacion() != null ? a.getCotizacion().getId() : null,
                a.getFechaEvento(), a.getHoraInicio(), a.getHoraFin(),
                a.getCostoTotal(), a.getEstado(), a.getObservaciones());
    }
}