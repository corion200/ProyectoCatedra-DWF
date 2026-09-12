package sv.edu.udb.cfc.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando se viola una regla de negocio
 * (cupo agotado, código duplicado, transición de estado inválida, etc.).
 * Capturada por GlobalExceptionHandler → responde 400 o 409 según el caso.
 *
 * Por defecto usa 409 CONFLICT (duplicados y conflictos de estado).
 * Pasa HttpStatus.BAD_REQUEST explícitamente para reglas de entrada inválidas.
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(String mensaje) {
        this(mensaje, HttpStatus.CONFLICT);
    }

    public BusinessException(String mensaje, HttpStatus status) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}