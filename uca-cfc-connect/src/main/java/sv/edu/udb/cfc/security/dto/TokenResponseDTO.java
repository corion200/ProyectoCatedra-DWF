package sv.edu.udb.cfc.security.dto;

public record TokenResponseDTO(
        String token,
        String tokenType,   // "Bearer"
        String correo,
        String rol) {
}
