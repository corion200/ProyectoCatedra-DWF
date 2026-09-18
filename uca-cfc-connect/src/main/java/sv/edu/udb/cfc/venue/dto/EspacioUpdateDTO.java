package sv.edu.udb.cfc.venue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import sv.edu.udb.cfc.venue.enums.TipoEspacio;

import java.math.BigDecimal;

public record EspacioUpdateDTO(
        @NotBlank @Size(max = 20) String codigo,
        @NotBlank @Size(max = 150) String nombre,
        @NotNull(message = "El tipo de espacio es obligatorio") TipoEspacio tipo,
        @NotNull @Positive(message = "La capacidad debe ser mayor a 0") Integer capacidad,
        @NotNull @PositiveOrZero(message = "El precio no puede ser negativo") BigDecimal precioHora,
        @Size(max = 1000) String equipamiento,
        Boolean disponible,
        Boolean activo) {
}