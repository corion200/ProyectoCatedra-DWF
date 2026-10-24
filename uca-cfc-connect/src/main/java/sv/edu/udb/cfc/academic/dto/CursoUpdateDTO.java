package sv.edu.udb.cfc.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import sv.edu.udb.cfc.academic.enums.TipoOferta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record CursoUpdateDTO(
        @NotBlank(message = "Debe ingresar el código del curso")
        @Size(max = 20) String codigo,

        @NotBlank(message = "Debe ingresar el nombre del curso")
        @Size(max = 200) String nombre,

        @Size(max = 1000) String descripcion,

        @NotNull(message = "Debe seleccionar el tipo de oferta") TipoOferta tipo,

        @NotNull(message = "Debe seleccionar una categoría") Long categoriaId,

        @NotNull(message = "Debe seleccionar una modalidad") Long modalidadId,

        @NotNull @Positive(message = "La cantidad de horas debe ser mayor a 0") Integer horas,

        @NotNull @PositiveOrZero(message = "El precio no puede ser un valor negativo") BigDecimal precio,

        @NotNull @Positive(message = "El cupo máximo debe ser mayor a 0") Integer cupoMaximo,

        @NotNull(message = "Debe indicar la fecha de inicio") LocalDate fechaInicio,

        @NotNull(message = "Debe indicar la fecha de finalización") LocalDate fechaFin,

        @NotEmpty(message = "Debe asignar como mínimo un docente") Set<Long> docentesIds,

        Boolean activo) {
}