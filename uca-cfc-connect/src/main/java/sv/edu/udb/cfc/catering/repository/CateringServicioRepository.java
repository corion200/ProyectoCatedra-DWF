package sv.edu.udb.cfc.catering.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import sv.edu.udb.cfc.catering.entity.CateringServicio;
import sv.edu.udb.cfc.catering.enums.EstadoCatering;

import java.time.LocalDate;
import java.util.List;

// JpaSpecificationExecutor permite armar filtros dinámicos con Specification
public interface CateringServicioRepository extends JpaRepository<CateringServicio, Long>, JpaSpecificationExecutor<CateringServicio> {

    // para validar que el código no esté repetido (sin importar mayúsculas)
    boolean existsByCodigoIgnoreCase(String codigo);

    // listado paginado por estado
    Page<CateringServicio> findByEstado(EstadoCatering estado, Pageable pageable);

    // agenda de entregas de un día (cocina/logística)
    Page<CateringServicio> findByFechaEvento(LocalDate fechaEvento, Pageable pageable);

    // servicios del día excluyendo un estado (ej. los cancelados)
    List<CateringServicio> findByFechaEventoAndEstadoNot(LocalDate fechaEvento, EstadoCatering estado);
}