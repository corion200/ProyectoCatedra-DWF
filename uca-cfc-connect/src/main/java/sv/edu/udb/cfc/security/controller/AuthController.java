package sv.edu.udb.cfc.security.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.cfc.security.dto.CambiarPasswordDTO;
import sv.edu.udb.cfc.security.dto.LoginRequestDTO;
import sv.edu.udb.cfc.security.dto.TokenResponseDTO;
import sv.edu.udb.cfc.security.service.AuthService;

import java.util.Map;

@Tag(name = "Autenticación (Módulo 9 Seguridad)", description = "Login, cambio y recuperación de contraseña con JWT")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login: valida credenciales y emite el token (indica si debe cambiar contraseña)")
    @PostMapping("/login")
    public TokenResponseDTO login(@Valid @RequestBody LoginRequestDTO dto) {
        return authService.login(dto);
    }

    @Operation(summary = "Cambia la contraseña del usuario autenticado (obligatorio si es temporal)")
    @ApiResponse(responseCode = "204", description = "Contraseña actualizada")
    @PatchMapping("/cambiar-password")
    public ResponseEntity<Void> cambiarPassword(@Valid @RequestBody CambiarPasswordDTO dto,
                                                Authentication llamador) {
        authService.cambiarPassword(llamador, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Logout (JWT stateless: el cliente descarta el token)")
    @PostMapping("/logout")
    public Map<String, String> logout() {
        return Map.of("mensaje",
                "Sesión finalizada. Con JWT stateless el cierre es del lado del cliente: descarte el token.");
    }

    @Operation(summary = "Recuperación: genera contraseña TEMPORAL y exige su cambio en el próximo login")
    @PostMapping("/recuperar")
    public Map<String, String> recuperar(@RequestParam String correo) {
        String temporal = authService.recuperar(correo);
        return Map.of("mensaje",
                "Se generó una contraseña temporal (en producción se envía por email). Deberá cambiarla al iniciar sesión.",
                "contrasenaTemporal", temporal);
    }
}