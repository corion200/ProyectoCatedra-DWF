package sv.edu.udb.cfc.common.enums;

/**
 * =====================================================================================
 * CLASE ENUMERADA: TipoServicio
 * =====================================================================================
 * Descripción general:
 * Clasifica la tipología de los servicios ofrecidos por la institución y gestionados
 * a través de la plataforma (tales como programas académicos, alquiler de espacios,
 * servicios de catering o consultorías especializadas).
 *
 * Sirve como eje transversal para la tarificación, cotización y asignación de
 * recursos específicos en los módulos del sistema.
 *
 * Notas de mantenimiento:
 * - Mantener sincronizado con los catálogos de la base de datos relacional.
 * =====================================================================================
 */
public enum TipoServicio {
    /**
     * Servicios de alimentación, banquetes, coffee breaks y atención gastronómica
     * provistos para eventos institucionales o externos.
     */
    COFFEE_BREAK,
    ALMUERZO,
    CENA,
    REFRIGERIO,
    BUFFET
}
