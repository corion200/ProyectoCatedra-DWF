package sv.edu.udb.cfc.venue.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sv.edu.udb.cfc.venue.dto.AlquilerCreateDTO;
import sv.edu.udb.cfc.venue.dto.AlquilerResponseDTO;
import sv.edu.udb.cfc.venue.dto.AlquilerUpdateDTO;
import sv.edu.udb.cfc.venue.enums.EstadoAlquiler;

import java.time.LocalDate;
import java.util.List;

public interface AlquilerService {

    AlquilerResponseDTO crear(AlquilerCreateDTO dto);

    AlquilerResponseDTO actualizar(Long id, AlquilerUpdateDTO dto);

    AlquilerResponseDTO obtenerPorId(Long id);

    Page<AlquilerResponseDTO> listar(Long clienteId, Long espacioId,
                                     EstadoAlquiler estado, LocalDate fecha, Pageable pageable);

    /** Horarios OCUPADOS de un espacio en una fecha (para mostrar disponibilidad al cliente). */
    List<AlquilerResponseDTO> ocupacionDeEspacio(Long espacioId, LocalDate fecha);

    AlquilerResponseDTO confirmar(Long id);

    AlquilerResponseDTO finalizar(Long id);

    AlquilerResponseDTO cancelar(Long id, String motivo);

    void eliminar(Long id);
}