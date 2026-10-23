package sv.edu.udb.cfc.common.enums;

/**
 * =====================================================================================
 * CLASE ENUMERADA: EstadoInscripcion
 * =====================================================================================
 * Descripción general:
 * Controla el ciclo de vida de los registros de inscripción de los participantes
 * o estudiantes en los diferentes cursos, diplomados y capacitaciones ofrecidas
 * por la plataforma institucional.
 *
 * Permite gestionar de forma centralizada los permisos de acceso a las aulas virtuales,
 * la validación de pagos y la confirmación definitiva de cupos disponibles.
 *
 * Notas técnicas y de arquitectura:
 * - Utilizado fuertemente en el módulo de inscripciones (enrollment).
 * - Diseñado para soportar transiciones síncronas ante confirmaciones bancarias.
 * =====================================================================================
 */
public enum EstadoInscripcion {
    /**
     * La solicitud de inscripción ha sido registrada en el sistema, pero se encuentra
     * a la espera de la validación del comprobante de pago o de la aprobación de
     * los requisitos previos por parte del departamento administrativo.
     */
    PENDIENTE,
    /**
     * La inscripción ha sido completada exitosamente. El participante cuenta con el
     * cupo asegurado y los permisos activos para cursar la formación académica.
     */
    CONFIRMADA,
    PAGADA,
    /**
     * El proceso de inscripción ha sido cancelado, ya sea por solicitud expresa del
     * participante o por el vencimiento del plazo estipulado para efectuar el pago
     * de la matrícula o arancel correspondiente.
     */
    CANCELADA,
    FINALIZADA
}
