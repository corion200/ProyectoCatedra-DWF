package sv.edu.udb.cfc.venue.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO de entrada para registrar una nueva reserva (alquiler) de un espacio.
 * Las validaciones se aplican con Bean Validation en el controlador.
 *
 * @param espacioId     identificador del espacio a reservar (obligatorio)
 * @param clienteId     identificador del cliente que reserva (obligatorio)
 * @param cotizacionId  identificador de la cotización asociada (opcional)
 * @param fechaEvento   fecha del evento; no puede ser en el pasado (obligatoria)
 * @param horaInicio    hora de inicio del evento (obligatoria)
 * @param horaFin       hora de fin del evento (obligatoria)
 * @param observaciones notas adicionales de la reserva, máximo 500 caracteres (opcional)
 */
public record AlquilerCreateDTO(
        @NotNull(message = "El espacio es obligatorio") Long espacioId,

        @NotNull(message = "El cliente es obligatorio") Long clienteId,

        Long cotizacionId,

        @NotNull(message = "La fecha del evento es obligatoria")
        @FutureOrPresent(message = "La fecha del evento no puede ser en el pasado") LocalDate fechaEvento,

        @NotNull(message = "La hora de inicio es obligatoria") LocalTime horaInicio,

        @NotNull(message = "La hora de fin es obligatoria") LocalTime horaFin,

        @Size(max = 500) String observaciones) {
}