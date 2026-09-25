package sv.edu.udb.cfc.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.cfc.security.dto.CambiarPasswordDTO;
import sv.edu.udb.cfc.security.dto.CrearUsuarioDTO;
import sv.edu.udb.cfc.security.dto.LoginRequestDTO;
import sv.edu.udb.cfc.security.dto.TokenResponseDTO;
import sv.edu.udb.cfc.security.dto.UsuarioResponseDTO;
import sv.edu.udb.cfc.security.entity.Rol;
import sv.edu.udb.cfc.security.entity.Usuario;
import sv.edu.udb.cfc.security.repository.RolRepository;
import sv.edu.udb.cfc.security.repository.UsuarioRepository;
import sv.edu.udb.cfc.shared.exception.BusinessException;
import sv.edu.udb.cfc.shared.exception.ResourceNotFoundException;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private static final SecureRandom RANDOM = new SecureRandom();

    public TokenResponseDTO login(LoginRequestDTO dto) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.correo(), dto.password()));
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        Usuario usuario = usuarioRepository.findByCorreo(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userDetails.getUsername()));
        return new TokenResponseDTO(jwtService.generarToken(userDetails), "Bearer",
                usuario.getCorreo(), usuario.getRol().getNombre(), usuario.getPasswordTemporal());
    }

    /** ADMIN crea usuarios de cualquier rol; RECEPCIONISTA solo puede crear CLIENTE. */
    @Transactional
    public UsuarioResponseDTO crearUsuario(CrearUsuarioDTO dto, Authentication llamador) {
        String correo = dto.correo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new BusinessException("Ya existe un usuario con el correo: " + correo);
        }
        String rolSolicitado = dto.rol().trim().toUpperCase();
        boolean esAdmin = llamador.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (!esAdmin && !"CLIENTE".equals(rolSolicitado)) {
            throw new BusinessException("Solo un ADMIN puede crear usuarios con roles administrativos",
                    HttpStatus.FORBIDDEN);
        }
        Rol rol = rolRepository.findByNombre(rolSolicitado)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", rolSolicitado));
        String temporal = generarPasswordTemporal();
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombre(dto.nombre().trim()).correo(correo)
                .password(passwordEncoder.encode(temporal))
                .passwordTemporal(true)      // ⭐ obliga al cambio en el próximo login
                .rol(rol).build());
        System.out.println("[CFC] Contraseña temporal para " + correo + ": " + temporal
                + " (en producción se envía por email)");
        return UsuarioResponseDTO.from(usuario);
    }

    /** Genera una contraseña temporal legible: CFC-XXXXXX (6 caracteres seguros). */
    public String generarPasswordTemporal() {
        String alfabeto = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder("CFC-");
        for (int i = 0; i < 6; i++) sb.append(alfabeto.charAt(RANDOM.nextInt(alfabeto.length())));
        return sb.toString();
    }

    /** El usuario autenticado cambia su propia contraseña (validando la actual). */
    @Transactional
    public void cambiarPassword(Authentication llamador, CambiarPasswordDTO dto) {
        Usuario usuario = usuarioRepository.findByCorreo(llamador.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", llamador.getName()));
        if (!passwordEncoder.matches(dto.passwordActual(), usuario.getPassword())) {
            throw new BusinessException("La contraseña actual no es correcta", HttpStatus.BAD_REQUEST);
        }
        if (passwordEncoder.matches(dto.passwordNueva(), usuario.getPassword())) {
            throw new BusinessException("La nueva contraseña debe ser diferente a la actual",
                    HttpStatus.BAD_REQUEST);
        }
        usuario.setPassword(passwordEncoder.encode(dto.passwordNueva()));
        usuario.setPasswordTemporal(false);   // ya no necesita cambio forzado
    }

    /** Recuperación: genera temporal, la marca como temporal → el próximo login exigirá el cambio. */
    @Transactional
    public String recuperar(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", correo));
        String temporal = generarPasswordTemporal();
        usuario.setPassword(passwordEncoder.encode(temporal));
        usuario.setPasswordTemporal(true);
        return temporal;
    }
}