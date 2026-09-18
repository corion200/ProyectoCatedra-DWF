package sv.edu.udb.cfc.venue.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import sv.edu.udb.cfc.venue.entity.Alquiler;
import sv.edu.udb.cfc.venue.enums.EstadoAlquiler;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface AlquilerRepository extends JpaRepository<Alquiler, Long>, JpaSpecificationExecutor<Alquiler> {
    boolean existsByCodigoIgnoreCase(String codigo);

    /**
     * Reservas ACTIVAS de un espacio en una fecha → base para detectar
     * solapamiento de horarios (se valida en el servicio comparando horas).
     */
    List<Alquiler> findByEspacioIdAndFechaEventoAndEstadoIn(
            Long espacioId, LocalDate fechaEvento, Collection<EstadoAlquiler> estados);

    Page<Alquiler> findByClienteId(Long clienteId, Pageable pageable);

    Page<Alquiler> findByEstado(EstadoAlquiler estado, Pageable pageable);
}