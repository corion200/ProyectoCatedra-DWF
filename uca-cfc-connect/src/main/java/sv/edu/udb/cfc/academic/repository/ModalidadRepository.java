package sv.edu.udb.cfc.academic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.cfc.academic.entity.Modalidad;

public interface ModalidadRepository extends JpaRepository<Modalidad, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    Page<Modalidad> findByActivoTrue(Pageable pageable);
}