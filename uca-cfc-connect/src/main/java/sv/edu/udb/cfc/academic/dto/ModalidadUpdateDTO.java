package sv.edu.udb.cfc.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ModalidadUpdateDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100) String nombre,
        @Size(max = 500) String descripcion,
        Boolean activo) {
}