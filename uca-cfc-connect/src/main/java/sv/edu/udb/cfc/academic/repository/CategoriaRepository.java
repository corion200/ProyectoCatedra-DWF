package sv.edu.udb.cfc.academic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.cfc.academic.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    Page<Categoria> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    Page<Categoria> findByActivoTrue(Pageable pageable);
}