package sv.edu.udb.cfc.common.enums;

/**
 * Representa los estados posibles del ciclo de vida de una Inscripción
 * (Módulo de Inscripciones).
 * <p><b>Nota:</b> el módulo de inscripciones usa su propio enum en enrollment.enums.
 */
public enum EstadoInscripcion {
    PENDIENTE,
    CONFIRMADA,
    PAGADA,
    CANCELADA,
    FINALIZADA
}
