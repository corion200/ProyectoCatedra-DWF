package sv.edu.udb.cfc.catering.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import sv.edu.udb.cfc.catering.enums.TipoServicioCatering;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record CateringCreateDTO(
        @NotNull(message = "El cliente es obligatorio") Long clienteId,
        Long cotizacionId,
        @NotNull(message = "El tipo de servicio es obligatorio") TipoServicioCatering tipoServicio,
        @NotNull(message = "El número de asistentes es obligatorio")
        @Positive(message = "Debe haber al menos 1 asistente") Integer numeroAsistentes,
        @NotBlank(message = "El menú es obligatorio")
        @Size(max = 1000, message = "El menú no puede exceder 1000 caracteres") String menu,
        @NotNull(message = "La fecha del evento es obligatoria")
        @FutureOrPresent(message = "La fecha del evento no puede ser en el pasado") LocalDate fechaEvento,
        @NotNull(message = "La hora de entrega es obligatoria") LocalTime horaEntrega,
        @NotBlank(message = "El lugar de entrega es obligatorio")
        @Size(max = 255) String lugar,
        @NotNull(message = "El precio por persona es obligatorio")
        @PositiveOrZero(message = "El precio no puede ser negativo") BigDecimal precioPorPersona,
        @Size(max = 500) String observaciones) {
}