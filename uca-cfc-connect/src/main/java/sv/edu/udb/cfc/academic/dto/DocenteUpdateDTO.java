package sv.edu.udb.cfc.academic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DocenteUpdateDTO(
        @NotBlank @Size(max = 100) String nombres,
        @NotBlank @Size(max = 100) String apellidos,
        @NotBlank @Pattern(regexp = "^\\d{8}-\\d$", message = "El DUI no tiene un formato válido (########-#)") String dui,
        @Size(max = 17) String nit,
        @NotBlank @Email(message = "El correo electrónico no es válido") @Size(max = 150) String correo,
        @Size(max = 15) String telefono,
        @Size(max = 200) String tituloProfesional,
        @Size(max = 200) String especialidad,
        Boolean activo) {
}