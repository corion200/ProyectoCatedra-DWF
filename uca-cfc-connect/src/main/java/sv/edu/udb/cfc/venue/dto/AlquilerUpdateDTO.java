package sv.edu.udb.cfc.venue.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

/** Si se cambian fecha/horas, el servicio re-valida solapamiento (excluyéndose a sí mismo). */
public record AlquilerUpdateDTO(
        @NotNull(message = "La fecha del evento es obligatoria")
        @FutureOrPresent(message = "La fecha del evento no puede ser en el pasado") LocalDate fechaEvento,
        @NotNull(message = "La hora de inicio es obligatoria") LocalTime horaInicio,
        @NotNull(message = "La hora de fin es obligatoria") LocalTime horaFin,
        @Size(max = 500) String observaciones) {
}