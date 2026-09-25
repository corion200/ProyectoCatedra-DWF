package sv.edu.udb.cfc.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** El rol NO se elige aquí: el registro público crea siempre CLIENTE. */
public record RegistroUsuarioDTO(
        @NotBlank @Size(max = 200) String nombre,
        @NotBlank @Email @Size(max = 150) String correo,
        @NotBlank @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") String password) {
}
