package sv.edu.udb.cfc.academic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import sv.edu.udb.cfc.academic.entity.Curso;

import java.util.Optional;

/**
 * Con JpaSpecificationExecutor se pueden armar filtros dinámicos y combinables
 * (nombre, tipo, categoría, estado...) mediante Specifications desde el servicio.
 */
public interface CursoRepository extends JpaRepository<Curso, Long>, JpaSpecificationExecutor<Curso> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    Optional<Curso> findByCodigoIgnoreCase(String codigo);

    /** Devuelve cuántos cursos usan la categoría indicada; sirve para impedir borrar categorías en uso. */
    long countByCategoriaId(Long categoriaId);

    /** Devuelve cuántos cursos usan la modalidad indicada; sirve para impedir borrar modalidades en uso. */
    long countByModalidadId(Long modalidadId);
}