package sv.edu.udb.cfc.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** No lleva contraseña: el sistema genera una TEMPORAL que el usuario deberá cambiar. */
public record CrearUsuarioDTO(
        @NotBlank @Size(max = 200) String nombre,
        @NotBlank @Email @Size(max = 150) String correo,
        @NotBlank String rol) {
}