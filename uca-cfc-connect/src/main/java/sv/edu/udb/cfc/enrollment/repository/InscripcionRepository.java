package sv.edu.udb.cfc.enrollment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import sv.edu.udb.cfc.enrollment.entity.Inscripcion;
import sv.edu.udb.cfc.enrollment.enums.EstadoInscripcion;

import java.util.Collection;
import java.util.List;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long>,
        JpaSpecificationExecutor<Inscripcion> {

    /** Regla de negocio: 1 inscripción por (cliente, curso) — respalda la UNIQUE de BD. */
    boolean existsByClienteIdAndCursoId(Long clienteId, Long cursoId);

    /** Cuenta inscripciones activas de un curso → control de cupo. */
    long countByCursoIdAndEstadoIn(Long cursoId, Collection<EstadoInscripcion> estados);

    List<Inscripcion> findByCursoIdAndEstado(Long cursoId, EstadoInscripcion estado);
}