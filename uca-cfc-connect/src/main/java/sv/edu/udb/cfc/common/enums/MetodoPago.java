package sv.edu.udb.cfc.common.enums;

/**
 * =====================================================================================
 * CLASE ENUMERADA: MetodoPago
 * =====================================================================================
 * Descripción general:
 * Especifica las formas de pago aceptadas y soportadas por la plataforma para el
 * procesamiento de inscripciones, cotizaciones y transacciones comerciales generales.
 *
 * Facilita la clasificación de los ingresos y la selección del canal correspondiente
 * al momento de registrar un pago manual o electrónico.
 *
 * Detalles de implementación:
 * - Extensible para futuras pasarelas digitales o métodos institucionales.
 * =====================================================================================
 */
public enum MetodoPago {
    /**
     * Pago en efectivo realizado directamente en las ventanillas o cajas habilitadas
     * por la institución para gestiones presenciales.
     */
    EFECTIVO,
    /**
     * Pago realizado mediante transferencia electrónica entre cuentas bancarias
     * institucionales o depósitos directos validados mediante comprobante digital.
     */
    TRANSFERENCIA,
    /**
     * Pago efectuado a través de tarjetas de crédito o débito integradas mediante
     * pasarelas de pago seguras en línea.
     */
    TARJETA_CREDITO,
    TARJETA_DEBITO,
    CHEQUE
}
