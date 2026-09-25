package sv.edu.udb.cfc.security.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.cfc.security.dto.CrearUsuarioDTO;
import sv.edu.udb.cfc.security.dto.UsuarioResponseDTO;
import sv.edu.udb.cfc.security.repository.UsuarioRepository;
import sv.edu.udb.cfc.security.service.AuthService;

import java.util.List;
import java.util.Map;

@Tag(name = "Usuarios (Módulo 9)", description = "Gestión de cuentas: creación con contraseña temporal y listado")
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final AuthService authService;
    private final UsuarioRepository usuarioRepository;

    @Operation(summary = "Crea un usuario con CONTRASEÑA TEMPORAL (ADMIN: cualquier rol; RECEPCIONISTA: solo CLIENTE)")
    @ApiResponse(responseCode = "201", description = "Usuario creado — devolver la temporal al responsable")
    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@Valid @RequestBody CrearUsuarioDTO dto,
                                                     Authentication llamador) {
        UsuarioResponseDTO usuario = authService.crearUsuario(dto, llamador);
        // En producción la temporal viaja por email; aquí se entrega al responsable para la demo.
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "usuario", usuario,
                "aviso", "Entregue la contraseña temporal al usuario; el sistema le exigirá cambiarla al iniciar sesión."));
    }

    @Operation(summary = "Lista todos los usuarios (solo ADMIN)")
    @GetMapping
    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findAll().stream().map(UsuarioResponseDTO::from).toList();
    }
}