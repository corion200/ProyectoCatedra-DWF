package sv.edu.udb.cfc.academic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DocenteCreateDTO(
        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 100) String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100) String apellidos,

        @NotBlank(message = "El DUI es obligatorio")
        @Pattern(regexp = "^\\d{8}-\\d$", message = "Formato de DUI inválido (########-#)") String dui,

        @Size(max = 17) String nit,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Correo electrónico inválido")
        @Size(max = 150) String correo,

        @Size(max = 15) String telefono,

        @Size(max = 200) String tituloProfesional,

        @Size(max = 200) String especialidad) {
}