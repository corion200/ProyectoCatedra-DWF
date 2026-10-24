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

// Si cambian asistentes o precio, se recalcula el costo total
public record CateringUpdateDTO(
        @NotNull(message = "Falta indicar el tipo de servicio") TipoServicioCatering tipoServicio, // Tipo de servicio
        @NotNull @Positive(message = "Debe haber al menos 1 asistente") Integer numeroAsistentes, // Cantidad de personas
        @NotBlank(message = "Falta indicar el menú") @Size(max = 1000) String menu, // Descripción del menú
        @NotNull(message = "Falta indicar la fecha del evento")
        @FutureOrPresent(message = "La fecha del evento no puede estar en el pasado") LocalDate fechaEvento, // Día del evento
        @NotNull(message = "Falta indicar la hora de entrega") LocalTime horaEntrega, // Hora de entrega
        @NotBlank(message = "Falta indicar el lugar de entrega") @Size(max = 255) String lugar, // Sitio de entrega
        @NotNull @PositiveOrZero(message = "El precio no puede ser negativo") BigDecimal precioPorPersona, // Precio por persona
        @Size(max = 500) String observaciones) { // Notas extra
}