package sv.edu.udb.cfc.quotation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import sv.edu.udb.cfc.quotation.entity.Cotizacion;
import sv.edu.udb.cfc.quotation.enums.EstadoCotizacion;
import sv.edu.udb.cfc.quotation.enums.TipoCotizacion;
/**
 * Acceso a datos de cotizaciones. Extiende {@code JpaRepository} (CRUD básico) y
 * {@code JpaSpecificationExecutor} (filtros dinámicos combinables, que usa el listado).
 * Los métodos {@code findBy...} son consultas que Spring genera a partir del nombre.
 */
public interface CotizacionRepository extends JpaRepository<Cotizacion, Long>, JpaSpecificationExecutor<Cotizacion> {

    boolean existsByCodigoIgnoreCase(String codigo);

    Page<Cotizacion> findByEstado(EstadoCotizacion estado, Pageable pageable);

    Page<Cotizacion> findByClienteId(Long clienteId, Pageable pageable);

    Page<Cotizacion> findByTipo(TipoCotizacion tipo, Pageable pageable);
}