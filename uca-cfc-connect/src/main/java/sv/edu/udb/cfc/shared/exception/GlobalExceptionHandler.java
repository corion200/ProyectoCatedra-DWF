package sv.edu.udb.cfc.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import sv.edu.udb.cfc.shared.model.ErrorResponse;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manejo centralizado de excepciones para TODA la API.
 * Garantiza que todo error devuelva la estructura uniforme ErrorResponse
 * (timestamp, status, error, message, path, detalles).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 404 – Recurso no encontrado (entidad buscada por id inexistente). */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    /** 400/409 – Regla de negocio violada (cupo agotado, duplicados, transición inválida...). */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest req) {
        return build(ex.getStatus(), ex.getMessage(), req, null);
    }

    /** 400 – Validaciones de DTOs (@Valid): devuelve mapa campo → mensaje. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest req) {
        Map<String, String> errores = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> Optional.ofNullable(fe.getDefaultMessage()).orElse("valor inválido"),
                        (a, b) -> a,
                        LinkedHashMap::new));
        return build(HttpStatus.BAD_REQUEST, "La solicitud contiene datos inválidos", req, errores);
    }

    /** 400 – Validaciones sobre @RequestParam / @PathVariable. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException ex,
                                                          HttpServletRequest req) {
        Map<String, String> errores = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        v -> Optional.ofNullable(v.getMessage()).orElse("valor inválido"),
                        (a, b) -> a));
        return build(HttpStatus.BAD_REQUEST, "Parámetros de la solicitud inválidos", req, errores);
    }

    /** 400 – Enum o tipo inválido en la URL (ej: PATCH /cursos/1/estado/INVENTADO). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest req) {
        String valoresAceptados = ex.getRequiredType() != null && ex.getRequiredType().isEnum()
                ? Arrays.toString(ex.getRequiredType().getEnumConstants())
                : ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconocido";
        String mensaje = String.format(
                "El valor '%s' no es válido para el parámetro '%s'. Valores aceptados: %s",
                ex.getValue(), ex.getName(), valoresAceptados);
        return build(HttpStatus.BAD_REQUEST, mensaje, req, null);
    }

    /** 400 – JSON malformado o con tipos incorrectos en el body. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex,
                                                          HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud es inválido o está mal formado", req, null);
    }

    /** 409 – Violación de UNIQUE/FK a nivel BD (red de seguridad). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegrity(DataIntegrityViolationException ex,
                                                         HttpServletRequest req) {
        log.warn("Violación de integridad: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "La operación viola una restricción de integridad de datos", req, null);
    }

    /** 500 – Error no controlado. No expone detalles internos al cliente. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno. Contacte al administrador", req, null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message,
                                                HttpServletRequest req, Map<String, String> detalles) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(status, message, req.getRequestURI(), detalles));
    }
}