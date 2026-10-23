package sv.edu.udb.cfc.common.enums;

/**
 * =====================================================================================
 * CLASE ENUMERADA: EstadoPago
 * =====================================================================================
 * Descripción general:
 * Define los estados financieros asociados a las transacciones monetarias registradas
 * en el módulo de pagos (payment) de la plataforma.
 *
 * Este catálogo es fundamental para garantizar la reconciliación contable y evitar
 * inconsistencias en la habilitación de servicios académicos o de alquiler de espacios.
 *
 * Especificaciones de diseño:
 * - Integración con pasarelas de pago y registros manuales de transferencias.
 * - Base para la generación de reportes financieros automáticos.
 * =====================================================================================
 */
public enum EstadoPago {
    /**
     * La transacción o factura ha sido generada en el sistema, pero el comprobante
     * de pago aún no ha sido reportado, recibido o verificado por la entidad.
     */
    PENDIENTE,
    /**
     * El pago ha sido verificado, procesado y acreditado satisfactoriamente, liberando
     * de manera automática los beneficios o servicios asociados en el sistema.
     */
    APROBADO,
    /**
     * La transacción monetaria ha fallado (por rechazo de tarjeta, fondos insuficientes
     * o error en la pasarela de pagos) o ha sido reversada administrativamente.
     */
    RECHAZADO,
    REEMBOLSADO
}
