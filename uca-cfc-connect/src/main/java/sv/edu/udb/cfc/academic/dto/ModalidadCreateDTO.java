package sv.edu.udb.cfc.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ModalidadCreateDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres") String nombre,
        @Size(max = 500, message = "La descripción no puede exceder 500 caracteres") String descripcion) {
}