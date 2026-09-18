package sv.edu.udb.cfc.payment.dto;

import java.math.BigDecimal;

/** Estado de cuenta de una inscripción: precio del curso vs pagos aplicados. */
public record ResumenPagoDTO(
        Long inscripcionId,
        BigDecimal precioCurso,
        BigDecimal totalPagado,
        BigDecimal saldoPendiente) {
}