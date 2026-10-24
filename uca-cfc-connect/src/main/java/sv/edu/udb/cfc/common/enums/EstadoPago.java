package sv.edu.udb.cfc.common.enums;

/**
 * Representa los estados posibles de un Pago (Módulo de Pagos).
 * <p><b>Nota:</b> el módulo de pagos usa su propio enum en payment.enums.
 */
public enum EstadoPago {
    PENDIENTE,
    APROBADO,
    RECHAZADO,
    REEMBOLSADO
}
