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
        @NotBlank(message = "El código es obligatorio")
        @Size(max = 20) String codigo,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 200) String nombre,

        @Size(max = 1000) String descripcion,

        @NotNull(message = "El tipo es obligatorio") TipoOferta tipo,

        @NotNull(message = "La categoría es obligatoria") Long categoriaId,

        @NotNull(message = "La modalidad es obligatoria") Long modalidadId,

        @NotNull @Positive(message = "Las horas deben ser mayores a 0") Integer horas,

        @NotNull @PositiveOrZero(message = "El precio no puede ser negativo") BigDecimal precio,

        @NotNull @Positive(message = "El cupo debe ser mayor a 0") Integer cupoMaximo,

        @NotNull(message = "La fecha de inicio es obligatoria") LocalDate fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria") LocalDate fechaFin,

        @NotEmpty(message = "Debe asignar al menos un docente") Set<Long> docentesIds,

        Boolean activo) {
}