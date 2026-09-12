package sv.edu.udb.cfc.shared.exception;

/**
 * Se lanza cuando no se encuentra un recurso solicitado.
 * Capturada por GlobalExceptionHandler → responde HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }

    /** Constructor de convenio: ResourceNotFoundException("Curso", 99)
     *  → "Curso con id '99' no fue encontrado" */
    public ResourceNotFoundException(String recurso, Object id) {
        super(String.format("%s con id '%s' no fue encontrado", recurso, id));
    }
}