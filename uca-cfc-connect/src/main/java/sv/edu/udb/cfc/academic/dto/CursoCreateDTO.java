package sv.edu.udb.cfc.academic.dto;

import jakarta.validation.constraints.FutureOrPresent;
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

public record CursoCreateDTO(
        @NotBlank(message = "El código es obligatorio")
        @Size(max = 20, message = "El código no puede exceder 20 caracteres") String codigo,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 200, message = "El nombre no puede exceder 200 caracteres") String nombre,

        @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres") String descripcion,

        @NotNull(message = "El tipo es obligatorio (CURSO o DIPLOMADO)") TipoOferta tipo,

        @NotNull(message = "La categoría es obligatoria") Long categoriaId,

        @NotNull(message = "La modalidad es obligatoria") Long modalidadId,

        @NotNull(message = "Las horas son obligatorias")
        @Positive(message = "Las horas deben ser mayores a 0") Integer horas,

        @NotNull(message = "El precio es obligatorio")
        @PositiveOrZero(message = "El precio no puede ser negativo") BigDecimal precio,

        @NotNull(message = "El cupo máximo es obligatorio")
        @Positive(message = "El cupo debe ser mayor a 0") Integer cupoMaximo,

        @NotNull(message = "La fecha de inicio es obligatoria")
        @FutureOrPresent(message = "La fecha de inicio no puede ser en el pasado") LocalDate fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria") LocalDate fechaFin,

        @NotEmpty(message = "Debe asignar al menos un docente") Set<Long> docentesIds) {
}