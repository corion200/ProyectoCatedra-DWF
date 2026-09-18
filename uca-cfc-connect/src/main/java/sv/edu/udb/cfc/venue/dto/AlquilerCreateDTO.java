package sv.edu.udb.cfc.venue.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

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