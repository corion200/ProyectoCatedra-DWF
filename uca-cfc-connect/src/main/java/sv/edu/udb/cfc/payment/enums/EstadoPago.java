package sv.edu.udb.cfc.payment.enums;

/** Flujo: PENDIENTE → PARCIAL → PAGADO. No se regresa a PENDIENTE. */
public enum EstadoPago {
    PENDIENTE,
    PARCIAL,
    PAGADO
}