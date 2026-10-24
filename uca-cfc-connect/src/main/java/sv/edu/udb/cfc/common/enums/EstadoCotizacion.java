package sv.edu.udb.cfc.common.enums;

/**
 * Representa los estados posibles del ciclo de vida de una Cotización
 * (Módulo de Cotizaciones).
 * <p><b>Nota:</b> el módulo de cotizaciones usa su propio enum en quotation.enums.
 */
public enum EstadoCotizacion {
    BORRADOR,
    ENVIADA,
    ACEPTADA,
    RECHAZADA,
    VENCIDA
}
