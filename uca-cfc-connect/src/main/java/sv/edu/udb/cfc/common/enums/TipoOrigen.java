package sv.edu.udb.cfc.common.enums;

/**
 * =====================================================================================
 * CLASE ENUMERADA: TipoOrigen
 * =====================================================================================
 * Descripción general:
 * Identifica la procedencia o el canal de origen desde el cual se genera una solicitud,
 * registro o interacción dentro del ecosistema de sistemas integrados.
 *
 * Permite realizar análisis estadísticos y auditorías sobre cómo los usuarios e
 * instituciones acceden y consumen los servicios del sistema.
 *
 * Consideraciones arquitectónicas:
 * - Utilizado en logs de auditoría y trazas de transacciones centralizadas.
 * =====================================================================================
 */
public enum TipoOrigen {
    INSCRIPCION,
    ALQUILER,
    CATERING
}
