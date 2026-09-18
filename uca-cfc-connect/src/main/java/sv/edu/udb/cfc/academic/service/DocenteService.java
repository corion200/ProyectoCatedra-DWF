package sv.edu.udb.cfc.academic.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sv.edu.udb.cfc.academic.dto.DocenteCreateDTO;
import sv.edu.udb.cfc.academic.dto.DocenteResponseDTO;
import sv.edu.udb.cfc.academic.dto.DocenteUpdateDTO;

public interface DocenteService {

    DocenteResponseDTO crear(DocenteCreateDTO dto);

    DocenteResponseDTO actualizar(Long id, DocenteUpdateDTO dto);

    DocenteResponseDTO obtenerPorId(Long id);

    /** busqueda: filtra por nombres O apellidos (contiene, ignora mayúsculas). */
    Page<DocenteResponseDTO> listar(String busqueda, Boolean activo, Pageable pageable);

    void eliminar(Long id);
}