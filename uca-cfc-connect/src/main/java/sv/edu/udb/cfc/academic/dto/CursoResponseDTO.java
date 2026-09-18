package sv.edu.udb.cfc.academic.dto;

import sv.edu.udb.cfc.academic.entity.Curso;
import sv.edu.udb.cfc.academic.enums.EstadoCurso;
import sv.edu.udb.cfc.academic.enums.TipoOferta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public record CursoResponseDTO(
        Long id, String codigo, String nombre, String descripcion,
        TipoOferta tipo, Long categoriaId, String categoriaNombre,
        Long modalidadId, String modalidadNombre,
        Integer horas, BigDecimal precio, Integer cupoMaximo,
        LocalDate fechaInicio, LocalDate fechaFin,
        EstadoCurso estado, Boolean activo, Set<DocenteItemDTO> docentes) {

    /** Requiere transacción activa: accede a relaciones LAZY (categoria, modalidad, docentes). */
    public static CursoResponseDTO from(Curso c) {
        return new CursoResponseDTO(c.getId(), c.getCodigo(), c.getNombre(), c.getDescripcion(),
                c.getTipo(), c.getCategoria().getId(), c.getCategoria().getNombre(),
                c.getModalidad().getId(), c.getModalidad().getNombre(),
                c.getHoras(), c.getPrecio(), c.getCupoMaximo(),
                c.getFechaInicio(), c.getFechaFin(), c.getEstado(), c.getActivo(),
                c.getDocentes().stream()
                        .map(d -> new DocenteItemDTO(d.getId(), d.getNombres() + " " + d.getApellidos()))
                        .collect(Collectors.toCollection(LinkedHashSet::new)));
    }
}