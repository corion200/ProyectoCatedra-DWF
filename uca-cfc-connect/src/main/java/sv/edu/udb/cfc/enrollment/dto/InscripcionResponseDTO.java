package sv.edu.udb.cfc.enrollment.dto;

import sv.edu.udb.cfc.enrollment.entity.Inscripcion;
import sv.edu.udb.cfc.enrollment.enums.EstadoInscripcion;

import java.time.LocalDateTime;

public record InscripcionResponseDTO(
        Long id, String codigo, Long clienteId, String clienteNombre,
        Long cursoId, String cursoNombre, EstadoInscripcion estado,
        LocalDateTime fechaInscripcion, String observaciones) {

    public static InscripcionResponseDTO from(Inscripcion i) {
        return new InscripcionResponseDTO(i.getId(), i.getCodigo(),
                i.getCliente().getId(), i.getCliente().getNombre(),
                i.getCurso().getId(), i.getCurso().getNombre(),
                i.getEstado(), i.getFechaInscripcion(), i.getObservaciones());
    }
}