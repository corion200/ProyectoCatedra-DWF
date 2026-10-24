package sv.edu.udb.cfc.security.dto;
/** Respuesta del login. Si {@code debeCambiarPassword} es true, el cliente debe forzar el cambio de contraseña. */
public record TokenResponseDTO(
        String token,
        String tokenType,
        String correo,
        String rol,
        Boolean debeCambiarPassword) {
}