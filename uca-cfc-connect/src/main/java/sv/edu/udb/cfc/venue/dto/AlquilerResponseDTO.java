package sv.edu.udb.cfc.venue.dto;

import sv.edu.udb.cfc.venue.entity.Alquiler;
import sv.edu.udb.cfc.venue.enums.EstadoAlquiler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record AlquilerResponseDTO(
        Long id, String codigo, Long espacioId, String espacioNombre,
        Long clienteId, String clienteNombre, Long cotizacionId,
        LocalDate fechaEvento, LocalTime horaInicio, LocalTime horaFin,
        BigDecimal costoTotal, EstadoAlquiler estado, String observaciones) {

    public static AlquilerResponseDTO from(Alquiler a) {
        return new AlquilerResponseDTO(a.getId(), a.getCodigo(),
                a.getEspacio().getId(), a.getEspacio().getNombre(),
                a.getCliente().getId(), a.getCliente().getNombre(),
                a.getCotizacion() != null ? a.getCotizacion().getId() : null,
                a.getFechaEvento(), a.getHoraInicio(), a.getHoraFin(),
                a.getCostoTotal(), a.getEstado(), a.getObservaciones());
    }
}