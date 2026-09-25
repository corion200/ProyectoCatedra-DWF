package sv.edu.udb.cfc.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sv.edu.udb.cfc.security.entity.Usuario;
import sv.edu.udb.cfc.security.repository.UsuarioRepository;

/** Puente entre Spring Security y nuestra tabla usuarios. */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));
        return User.builder()
                .username(usuario.getCorreo())
                .password(usuario.getPassword())          // hash BCrypt — Spring lo compara
                .authorities("ROLE_" + usuario.getRol().getNombre())
                .disabled(!Boolean.TRUE.equals(usuario.getActivo()))
                .build();
    }
}
