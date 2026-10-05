package sv.edu.udb.cfc.shared.exception;

/**
 * Excepción que se lanza cuando no se encuentra el recurso solicitado.
 * La atrapa GlobalExceptionHandler → responde HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }

    /** Constructor de conveniencia: ResourceNotFoundException("Curso", 99)
     *  → "Curso con id '99' no fue encontrado" */
    public ResourceNotFoundException(String recurso, Object id) {
        super(String.format("%s con id '%s' no fue encontrado", recurso, id));
    }
}