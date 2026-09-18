package sv.edu.udb.cfc.enrollment.dto;

import jakarta.validation.constraints.Size;

/**
 * Solo campos editables de una inscripción. El estado NUNCA se modifica
 * por aquí: se usa PATCH /{id}/confirmar y PATCH /{id}/cancelar.
 */
public record InscripcionUpdateDTO(
        @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres") String observaciones) {
}