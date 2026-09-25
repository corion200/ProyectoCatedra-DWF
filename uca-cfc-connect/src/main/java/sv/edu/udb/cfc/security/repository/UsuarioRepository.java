package sv.edu.udb.cfc.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.cfc.security.entity.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByCorreoIgnoreCase(String correo);
}
