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
    /** Crea la reserva (PENDIENTE) con el costo calculado. Falla si el horario se cruza con otra activa del espacio. */
    AlquilerResponseDTO crear(AlquilerCreateDTO dto);

    /** Reprograma una reserva: vuelve a validar solapamiento y recalcula el costo. */
    AlquilerResponseDTO actualizar(Long id, AlquilerUpdateDTO dto);

    AlquilerResponseDTO obtenerPorId(Long id);

    Page<AlquilerResponseDTO> listar(Long clienteId, Long espacioId,
                                     EstadoAlquiler estado, LocalDate fecha, Pageable pageable);

    /** Horarios OCUPADOS de un espacio en una fecha (para mostrar disponibilidad al cliente). */
    List<AlquilerResponseDTO> ocupacionDeEspacio(Long espacioId, LocalDate fecha);

    AlquilerResponseDTO confirmar(Long id);
    /** Cancela una reserva PENDIENTE o CONFIRMADA y libera el horario. El motivo, si se envía, reemplaza las observaciones. */
    AlquilerResponseDTO finalizar(Long id);

    /** Elimina solo reservas PENDIENTES; las demás deben cancelarse para conservar el historial. */
    AlquilerResponseDTO cancelar(Long id, String motivo);

    void eliminar(Long id);
}