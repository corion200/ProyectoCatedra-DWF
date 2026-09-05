package sv.edu.udb.cfc.common.enums;

/**
 * Catálogo fijo de métodos de pago aceptados por el sistema
 * (Módulo de Pagos). Al ser un catálogo cerrado y de bajo cambio,
 * se modela como enum en lugar de una entidad/tabla independiente.
 */
public enum MetodoPago {
    EFECTIVO,
    TRANSFERENCIA,
    TARJETA_CREDITO,
    TARJETA_DEBITO,
    CHEQUE
}
