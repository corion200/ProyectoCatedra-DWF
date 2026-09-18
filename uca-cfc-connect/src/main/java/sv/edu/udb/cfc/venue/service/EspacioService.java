package sv.edu.udb.cfc.venue.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sv.edu.udb.cfc.venue.dto.EspacioCreateDTO;
import sv.edu.udb.cfc.venue.dto.EspacioResponseDTO;
import sv.edu.udb.cfc.venue.dto.EspacioUpdateDTO;
import sv.edu.udb.cfc.venue.enums.TipoEspacio;

import java.util.List;

public interface EspacioService {

    EspacioResponseDTO crear(EspacioCreateDTO dto);

    EspacioResponseDTO actualizar(Long id, EspacioUpdateDTO dto);

    EspacioResponseDTO obtenerPorId(Long id);

    /** Filtros combinables: nombre (contiene), tipo, capacidad mínima y disponibilidad. */
    Page<EspacioResponseDTO> listar(String nombre, TipoEspacio tipo, Integer capacidadMinima,
                                    Boolean disponible, Pageable pageable);

    /** Catálogo público: espacios ofrecidos para alquiler. */
    List<EspacioResponseDTO> listarDisponibles();

    void eliminar(Long id);
}