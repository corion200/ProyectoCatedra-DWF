package sv.edu.udb.cfc.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.cfc.security.dto.LoginRequestDTO;
import sv.edu.udb.cfc.security.dto.RegistroUsuarioDTO;
import sv.edu.udb.cfc.security.dto.TokenResponseDTO;
import sv.edu.udb.cfc.security.entity.Rol;
import sv.edu.udb.cfc.security.entity.Usuario;
import sv.edu.udb.cfc.security.repository.RolRepository;
import sv.edu.udb.cfc.security.repository.UsuarioRepository;
import sv.edu.udb.cfc.shared.exception.BusinessException;
import sv.edu.udb.cfc.shared.exception.ResourceNotFoundException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public TokenResponseDTO login(LoginRequestDTO dto) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.correo(), dto.password()));
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        Usuario usuario = usuarioRepository.findByCorreo(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userDetails.getUsername()));
        return new TokenResponseDTO(jwtService.generarToken(userDetails), "Bearer",
                usuario.getCorreo(), usuario.getRol().getNombre());
    }

    @Transactional
    public TokenResponseDTO registro(RegistroUsuarioDTO dto) {
        if (usuarioRepository.existsByCorreoIgnoreCase(dto.correo().trim().toLowerCase())) {
            throw new BusinessException("Ya existe un usuario con el correo: " + dto.correo());
        }
        // REGLA: el registro público crea SIEMPRE rol CLIENTE (nunca un rol administrativo)
        Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "CLIENTE"));
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombre(dto.nombre().trim())
                .correo(dto.correo().trim().toLowerCase())
                .password(passwordEncoder.encode(dto.password()))
                .rol(rolCliente)
                .build());
        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getCorreo());
        return new TokenResponseDTO(jwtService.generarToken(userDetails), "Bearer",
                usuario.getCorreo(), usuario.getRol().getNombre());
    }

    /**
     * Recuperación de contraseña (Módulo 9). Simplificación de demo: genera una
     * contraseña temporal y la devuelve en la respuesta. En producción viajaría
     * por email con un token de restablecimiento de un solo uso.
     */
    @Transactional
    public String recuperar(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", correo));
        String temporal = UUID.randomUUID().toString().substring(0, 8) + "A!";
        usuario.setPassword(passwordEncoder.encode(temporal));
        return temporal;
    }
}
