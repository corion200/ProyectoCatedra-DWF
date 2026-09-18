package sv.edu.udb.cfc.academic.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sv.edu.udb.cfc.academic.dto.ModalidadCreateDTO;
import sv.edu.udb.cfc.academic.dto.ModalidadResponseDTO;
import sv.edu.udb.cfc.academic.dto.ModalidadUpdateDTO;

public interface ModalidadService {

    ModalidadResponseDTO crear(ModalidadCreateDTO dto);

    ModalidadResponseDTO actualizar(Long id, ModalidadUpdateDTO dto);

    ModalidadResponseDTO obtenerPorId(Long id);

    Page<ModalidadResponseDTO> listar(Boolean activo, Pageable pageable);

    void eliminar(Long id);
}