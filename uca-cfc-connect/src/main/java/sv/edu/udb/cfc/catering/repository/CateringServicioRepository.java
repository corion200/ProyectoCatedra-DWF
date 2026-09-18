package sv.edu.udb.cfc.catering.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import sv.edu.udb.cfc.catering.entity.CateringServicio;
import sv.edu.udb.cfc.catering.enums.EstadoCatering;

import java.time.LocalDate;

public interface CateringServicioRepository extends JpaRepository<CateringServicio, Long>, JpaSpecificationExecutor<CateringServicio> {
    boolean existsByCodigoIgnoreCase(String codigo);

    Page<CateringServicio> findByEstado(EstadoCatering estado, Pageable pageable);

    /** Agenda de entregas de un día (para cocina/logística). */
    Page<CateringServicio> findByFechaEvento(LocalDate fechaEvento, Pageable pageable);
}