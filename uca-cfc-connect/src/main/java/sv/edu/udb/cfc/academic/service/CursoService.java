package sv.edu.udb.cfc.academic.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sv.edu.udb.cfc.academic.dto.CursoCreateDTO;
import sv.edu.udb.cfc.academic.dto.CursoResponseDTO;
import sv.edu.udb.cfc.academic.dto.CursoUpdateDTO;
import sv.edu.udb.cfc.academic.enums.EstadoCurso;
import sv.edu.udb.cfc.academic.enums.TipoOferta;

public interface CursoService {

    CursoResponseDTO crear(CursoCreateDTO dto);

    CursoResponseDTO actualizar(Long id, CursoUpdateDTO dto);

    CursoResponseDTO obtenerPorId(Long id);

    /** Filtros combinables: nombre (contiene), tipo, categoria, modalidad y estado. */
    Page<CursoResponseDTO> listar(String nombre, TipoOferta tipo, Long categoriaId,
                                  Long modalidadId, EstadoCurso estado, Pageable pageable);

    /** Máquina de estados: solo permite transiciones válidas. */
    CursoResponseDTO cambiarEstado(Long id, EstadoCurso nuevoEstado);

    void eliminar(Long id);
}