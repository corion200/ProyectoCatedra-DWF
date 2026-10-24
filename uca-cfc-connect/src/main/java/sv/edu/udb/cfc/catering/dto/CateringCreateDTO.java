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

// Datos para registrar un servicio de catering
public record CateringCreateDTO(
        // Cliente que pide el servicio
        @NotNull(message = "Falta indicar el cliente") Long clienteId,
        // Cotización relacionada (opcional)
        Long cotizacionId,
        // Tipo de servicio: coffee break, almuerzo, etc.
        TipoServicioCatering tipoServicio,
        // Cantidad de personas
        @NotNull(message = "Falta indicar el número de asistentes")
        @Positive(message = "Debe haber al menos 1 asistente") Integer numeroAsistentes,
        // Descripción del menú
        @NotBlank(message = "Falta indicar el menú")
        @Size(max = 1000, message = "El menú no puede pasar de 1000 caracteres") String menu,
        // Día del evento
        @NotNull(message = "Falta indicar la fecha del evento")
        @FutureOrPresent(message = "La fecha del evento no puede estar en el pasado") LocalDate fechaEvento,
        // Hora en que se entrega la comida
        LocalTime horaEntrega,
        // Sitio de la entrega
        @Size(max = 255) String lugar,
        // Precio de cada persona
        @NotNull(message = "Falta indicar el precio por persona")
        @PositiveOrZero(message = "El precio no puede ser negativo") BigDecimal precioPorPersona,
        // Notas extra (opcional)
        @Size(max = 500) String observaciones) {
}