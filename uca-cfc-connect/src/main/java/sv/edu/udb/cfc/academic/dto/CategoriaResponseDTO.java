package sv.edu.udb.cfc.academic.dto;

import sv.edu.udb.cfc.academic.entity.Categoria;

import java.time.LocalDateTime;

public record CategoriaResponseDTO(
        Long id,
        String nombre,
        String descripcion,
        Boolean activo,
        LocalDateTime fechaCreacion) {

    public static CategoriaResponseDTO from(Categoria c) {
        return new CategoriaResponseDTO(c.getId(), c.getNombre(), c.getDescripcion(),
                c.getActivo(), c.getFechaCreacion());
    }
}