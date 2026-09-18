package sv.edu.udb.cfc.enrollment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InscripcionCreateDTO(
        @NotNull(message = "El cliente es obligatorio") Long clienteId,
        @NotNull(message = "El curso es obligatorio") Long cursoId,
        @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres") String observaciones) {
}