package sv.edu.udb.cfc.academic.dto;

import sv.edu.udb.cfc.academic.entity.Modalidad;

import java.time.LocalDateTime;

public record ModalidadResponseDTO(
        Long id, String nombre, String descripcion, Boolean activo, LocalDateTime fechaCreacion) {

    public static ModalidadResponseDTO from(Modalidad m) {
        return new ModalidadResponseDTO(m.getId(), m.getNombre(), m.getDescripcion(),
                m.getActivo(), m.getFechaCreacion());
    }
}