package sv.edu.udb.cfc.enrollment.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sv.edu.udb.cfc.enrollment.dto.InscripcionCreateDTO;
import sv.edu.udb.cfc.enrollment.dto.InscripcionResponseDTO;
import sv.edu.udb.cfc.enrollment.dto.InscripcionUpdateDTO;
import sv.edu.udb.cfc.enrollment.enums.EstadoInscripcion;

public interface InscripcionService {

    InscripcionResponseDTO inscribir(InscripcionCreateDTO dto);

    InscripcionResponseDTO actualizarObservaciones(Long id, InscripcionUpdateDTO dto);

    InscripcionResponseDTO confirmar(Long id);

    InscripcionResponseDTO cancelar(Long id, String motivo);

    InscripcionResponseDTO obtenerPorId(Long id);

    Page<InscripcionResponseDTO> listar(Long clienteId, Long cursoId,
                                        EstadoInscripcion estado, Pageable pageable);
}