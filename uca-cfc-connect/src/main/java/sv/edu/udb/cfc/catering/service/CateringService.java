package sv.edu.udb.cfc.catering.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sv.edu.udb.cfc.catering.dto.CateringCreateDTO;
import sv.edu.udb.cfc.catering.dto.CateringResponseDTO;
import sv.edu.udb.cfc.catering.dto.CateringUpdateDTO;
import sv.edu.udb.cfc.catering.enums.EstadoCatering;

import java.time.LocalDate;

public interface CateringService {

    CateringResponseDTO crear(CateringCreateDTO dto);

    CateringResponseDTO actualizar(Long id, CateringUpdateDTO dto);

    CateringResponseDTO obtenerPorId(Long id);

    Page<CateringResponseDTO> listar(Long clienteId, EstadoCatering estado,
                                     LocalDate fecha, Pageable pageable);

    CateringResponseDTO confirmar(Long id);

    CateringResponseDTO finalizar(Long id);

    CateringResponseDTO cancelar(Long id, String motivo);

    void eliminar(Long id);
}