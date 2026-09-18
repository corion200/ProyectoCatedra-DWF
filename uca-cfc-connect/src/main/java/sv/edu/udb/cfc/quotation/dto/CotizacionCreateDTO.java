package sv.edu.udb.cfc.quotation.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import sv.edu.udb.cfc.quotation.enums.TipoCotizacion;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CotizacionCreateDTO(
        @NotNull(message = "El cliente es obligatorio") Long clienteId,

        @NotNull(message = "El tipo de cotización es obligatorio") TipoCotizacion tipo,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres") String descripcion,

        @Future(message = "La fecha del evento debe ser futura") LocalDate fechaEvento,

        @NotNull(message = "El monto estimado es obligatorio")
        @PositiveOrZero(message = "El monto no puede ser negativo") BigDecimal montoEstimado,

        @Size(max = 500) String observaciones) {
}