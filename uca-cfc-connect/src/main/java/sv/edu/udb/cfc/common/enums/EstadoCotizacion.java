package sv.edu.udb.cfc.common.enums;

/**
 * =====================================================================================
 * CLASE ENUMERADA: EstadoCotizacion
 * =====================================================================================
 * Descripción general:
 * Este enum define de manera estricta los diferentes estados por los cuales puede
 * transicionar una cotización dentro del flujo del sistema UCA CFC Connect.
 *
 * Su propósito principal es mantener la integridad de los datos en el módulo de
 * cotizaciones (quotation), asegurando que las reglas de negocio referentes a la
 * aprobación, rechazo, vigencia o anulación de presupuestos se apliquen correctamente.
 *
 * Historial de cambios y documentación interna:
 * - Creación inicial del catálogo de estados para cotizaciones académicas y de servicios.
 * - Adición de validaciones lógicas para control de transiciones de estado.
 * - Documentación detallada de cada constante para auditoría y trazabilidad.
 * =====================================================================================
 */
public enum EstadoCotizacion {
    BORRADOR,
    /**
     * Representa una cotización que ha sido recién creada en el sistema por el usuario
     * o asesor comercial, pero que aún se encuentra pendiente de revisión interna
     * o de ser enviada formalmente al cliente solicitante.
     */
    ENVIADA,
    /**
     * Indica que la cotización ha sido aprobada formalmente por el cliente o por la
     * autoridad competente, habilitando el proceso de conversión hacia una orden de
     * servicio, inscripción o el trámite administrativo correspondiente.
     */
    ACEPTADA,
    /**
     * Señala que la cotización ha sido rechazada explícitamente por el cliente
     * (debido a costos, tiempos u otros factores), cerrando su ciclo de vida comercial
     * sin generar compromisos financieros o contractuales adicionales.
     */
    RECHAZADA,
    /**
     * Estado asignado automáticamente cuando una cotización excede el período de
     * vigencia establecido por las políticas institucionales (por ejemplo, después de
     * 30 días sin respuesta), requiriendo una reevaluación si se desea retomar.
     */
    VENCIDA
}
