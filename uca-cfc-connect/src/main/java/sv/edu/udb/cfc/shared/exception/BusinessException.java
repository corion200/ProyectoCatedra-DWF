package sv.edu.udb.cfc.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción que se lanza al incumplir una regla de negocio
 * (cupo agotado, código repetido, cambio de estado no válido, etc.).
 * La atrapa GlobalExceptionHandler → responde 400 o 409 según corresponda.
 *
 * Por defecto usa 409 CONFLICT (registros duplicados y conflictos de estado).
 * Se debe indicar HttpStatus.BAD_REQUEST de forma explícita cuando la regla
 * se refiere a datos de entrada inválidos.
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