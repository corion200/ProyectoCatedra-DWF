package sv.edu.udb.cfc.academic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import sv.edu.udb.cfc.academic.entity.Curso;

import java.util.Optional;

/**
 * JpaSpecificationExecutor habilita filtros dinámicos combinables
 * (nombre + tipo + categoría + estado...) vía Specifications en el servicio.
 */
public interface CursoRepository extends JpaRepository<Curso, Long>, JpaSpecificationExecutor<Curso> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    Optional<Curso> findByCodigoIgnoreCase(String codigo);

    /** Cuenta cursos que usan una categoría — evita eliminar categorías en uso. */
    long countByCategoriaId(Long categoriaId);

    /** Cuenta cursos que usan una modalidad — evita eliminar modalidades en uso. */
    long countByModalidadId(Long modalidadId);
}