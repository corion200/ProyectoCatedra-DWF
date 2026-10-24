package sv.edu.udb.cfc.security.dto;

import sv.edu.udb.cfc.security.entity.Usuario;

import java.time.LocalDateTime;
/**
 * Datos públicos de una cuenta (nunca incluye la contraseña). {@code rol} es el
 * nombre del rol y {@code debeCambiarPassword} es true mientras la contraseña sea temporal.
 */
public record UsuarioResponseDTO(
        Long id, String nombre, String correo, String rol, Boolean activo,
        Boolean debeCambiarPassword, LocalDateTime fechaCreacion) {

    public static UsuarioResponseDTO from(Usuario u) {
        return new UsuarioResponseDTO(u.getId(), u.getNombre(), u.getCorreo(),
                u.getRol().getNombre(), u.getActivo(), u.getPasswordTemporal(), u.getFechaCreacion());
    }
}