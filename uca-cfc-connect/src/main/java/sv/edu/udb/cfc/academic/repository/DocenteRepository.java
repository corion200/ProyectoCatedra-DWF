package sv.edu.udb.cfc.academic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.cfc.academic.entity.Docente;

public interface DocenteRepository extends JpaRepository<Docente, Long> {

    boolean existsByDui(String dui);

    boolean existsByDuiAndIdNot(String dui, Long id);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);

    Page<Docente> findByActivoTrue(Pageable pageable);

    /** Búsqueda por nombre O apellido (el mismo texto en ambos campos). */
    Page<Docente> findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
            String nombres, String apellidos, Pageable pageable);
}