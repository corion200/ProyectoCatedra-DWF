package sv.edu.udb.cfc.venue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import sv.edu.udb.cfc.venue.enums.TipoEspacio;

import java.math.BigDecimal;

/**
 * Datos para registrar un espacio. El {@code codigo} debe ser único
 * (si ya existe, el servicio responde 409).
 *
 * @param precioHora  tarifa por hora; puede ser 0 (espacio sin costo). El costo
 *                    de cada reserva se calcula a partir de este valor
 * @param disponible  opcional; si no se envía, el servicio define el valor por defecto
 */
public record EspacioCreateDTO(
        @NotBlank(message = "El código es obligatorio")
        @Size(max = 20) String codigo,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150) String nombre,

        @NotNull(message = "El tipo de espacio es obligatorio") TipoEspacio tipo,

        @NotNull(message = "La capacidad es obligatoria")
        @Positive(message = "La capacidad debe ser mayor a 0") Integer capacidad,

        @NotNull(message = "El precio por hora es obligatorio")
        @PositiveOrZero(message = "El precio no puede ser negativo") BigDecimal precioHora,

        @Size(max = 1000) String equipamiento,

        Boolean disponible) {
}