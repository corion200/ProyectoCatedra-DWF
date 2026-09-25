package sv.edu.udb.cfc.security.dto;

public record TokenResponseDTO(
        String token,
        String tokenType,
        String correo,
        String rol,
        Boolean debeCambiarPassword) {
}