package sv.edu.udb.cfc.security.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.cfc.security.dto.LoginRequestDTO;
import sv.edu.udb.cfc.security.dto.RegistroUsuarioDTO;
import sv.edu.udb.cfc.security.dto.TokenResponseDTO;
import sv.edu.udb.cfc.security.service.AuthService;

import java.util.Map;

@Tag(name = "Autenticación (Módulo 9 Seguridad)", description = "Login, registro, logout y recuperación de contraseña con JWT")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login: valida credenciales y emite el token JWT")
    @ApiResponse(responseCode = "200", description = "Token emitido")
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    @PostMapping("/login")
    public TokenResponseDTO login(@Valid @RequestBody LoginRequestDTO dto) {
        return authService.login(dto);
    }

    @Operation(summary = "Registro público: crea un usuario con rol CLIENTE y emite su token")
    @ApiResponse(responseCode = "201", description = "Usuario creado y autenticado")
    @PostMapping("/registro")
    public ResponseEntity<TokenResponseDTO> registro(@Valid @RequestBody RegistroUsuarioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registro(dto));
    }

    @Operation(summary = "Logout (JWT stateless: el cliente descarta el token)")
    @PostMapping("/logout")
    public Map<String, String> logout() {
        return Map.of("mensaje",
                "Sesión finalizada. Con JWT stateless el cierre es del lado del cliente: descarte el token.");
    }

    @Operation(summary = "Recuperación de contraseña: devuelve una contraseña temporal (demo)")
    @PostMapping("/recuperar")
    public Map<String, String> recuperar(@RequestParam String correo) {
        String temporal = authService.recuperar(correo);
        return Map.of("mensaje", "Contraseña temporal generada (en producción se envía por email).",
                "contrasenaTemporal", temporal);
    }
}
