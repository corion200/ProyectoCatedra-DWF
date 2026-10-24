package sv.edu.udb.cfc.quotation.enums;
/**
 * Estados de una cotización. Las transiciones permitidas son:
 * PENDIENTE → EN_PROCESO, APROBADA o RECHAZADA; EN_PROCESO → APROBADA o RECHAZADA.
 * APROBADA y RECHAZADA son estados finales (no se puede salir de ellos).
 * Ojo: el código también permite pasar de PENDIENTE directo a APROBADA/RECHAZADA,
 * sin pasar por EN_PROCESO.
 */
public enum EstadoCotizacion {
    PENDIENTE,
    EN_PROCESO,
    APROBADA,
    RECHAZADA
}