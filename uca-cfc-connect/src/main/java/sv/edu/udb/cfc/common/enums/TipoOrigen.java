package sv.edu.udb.cfc.common.enums;

/**
 * Identifica el módulo de negocio que origina un registro transversal
 * (por ejemplo, un Pago o una Cotización puede originarse desde una
 * Inscripción, un Alquiler de espacio o una Solicitud de Catering).
 * <p>
 * Permite que entidades como {@code Pago} o {@code Cotizacion} referencien
 * un origen polimórfico sin necesidad de una FK distinta por módulo.
 */
public enum TipoOrigen {
    INSCRIPCION,
    ALQUILER,
    CATERING
}
