package sv.edu.udb.cfc.academic.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sv.edu.udb.cfc.academic.dto.CategoriaCreateDTO;
import sv.edu.udb.cfc.academic.dto.CategoriaResponseDTO;
import sv.edu.udb.cfc.academic.dto.CategoriaUpdateDTO;

public interface CategoriaService {

    CategoriaResponseDTO crear(CategoriaCreateDTO dto);

    CategoriaResponseDTO actualizar(Long id, CategoriaUpdateDTO dto);

    CategoriaResponseDTO obtenerPorId(Long id);

    Page<CategoriaResponseDTO> listar(String nombre, Boolean activo, Pageable pageable);

    void eliminar(Long id);
}