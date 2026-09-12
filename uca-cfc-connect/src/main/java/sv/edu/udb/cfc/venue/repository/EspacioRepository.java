package sv.edu.udb.cfc.venue.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.cfc.venue.entity.Espacio;
import sv.edu.udb.cfc.venue.enums.TipoEspacio;

import java.util.List;

public interface EspacioRepository extends JpaRepository<Espacio, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    /** Espacios ofrecidos para alquiler (catálogo público). */
    List<Espacio> findByDisponibleTrueAndActivoTrue();

    Page<Espacio> findByTipo(TipoEspacio tipo, Pageable pageable);

    /** Filtro "busco espacio para N personas". */
    Page<Espacio> findByCapacidadGreaterThanEqual(Integer capacidad, Pageable pageable);
}