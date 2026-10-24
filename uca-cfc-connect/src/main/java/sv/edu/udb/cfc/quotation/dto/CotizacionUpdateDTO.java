package sv.edu.udb.cfc.quotation.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import sv.edu.udb.cfc.quotation.enums.TipoCotizacion;

import java.math.BigDecimal;
import java.time.LocalDate;
/**
 * Datos para editar una cotización (mismas validaciones que al crear).
 * El estado NO se cambia por aquí, sino con PATCH {@code /{id}/estado/{estado}}.
 */
public record CotizacionUpdateDTO(
        @NotNull(message = "El cliente es obligatorio") Long clienteId,
        @NotNull(message = "El tipo de cotización es obligatorio") TipoCotizacion tipo,
        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 1000) String descripcion,
        @Future(message = "La fecha del evento debe ser futura") LocalDate fechaEvento,
        @NotNull @PositiveOrZero(message = "El monto no puede ser negativo") BigDecimal montoEstimado,
        @Size(max = 500) String observaciones) {
}