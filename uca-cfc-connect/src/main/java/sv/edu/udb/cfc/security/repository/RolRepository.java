package sv.edu.udb.cfc.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.cfc.security.entity.Rol;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(String nombre);
}
