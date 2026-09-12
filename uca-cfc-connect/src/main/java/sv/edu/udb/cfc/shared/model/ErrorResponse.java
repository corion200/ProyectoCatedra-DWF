package sv.edu.udb.cfc.shared.model;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Estructura uniforme de respuesta de error para TODA la API.
 * Campos: timestamp, status, error, message, path y detalles opcionales
 * (mapa campo → mensaje en errores de validación).
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