package sv.edu.udb.cfc.shared.model;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato único de respuesta de error para toda la API.
 * Incluye: timestamp, status, error, message, path y, de forma opcional,
 * un mapa campo → mensaje con el detalle de los errores de validación.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> detalles) {

    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                null);
    }

    public static ErrorResponse of(HttpStatus status, String message, String path, Map<String, String> detalles) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                detalles);
    }
}