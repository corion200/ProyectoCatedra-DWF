package sv.edu.udb.cfc.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import sv.edu.udb.cfc.payment.enums.MetodoPago;

import java.math.BigDecimal;

public record PagoCreateDTO(
        Long inscripcionId,     // Exactamente UNO de los 4 debe venir con valor
        Long cotizacionId,
        Long alquilerId,
        Long cateringId,

        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor a 0") BigDecimal monto,

        @NotNull(message = "El método de pago es obligatorio") MetodoPago metodoPago,

        @Size(max = 50) String numeroComprobante,

        @Size(max = 500) String observaciones) {
}