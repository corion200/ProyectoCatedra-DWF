package sv.edu.udb.cfc.agenda.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/** Una fila del calendario institucional: cualquier actividad del centro en una fecha. */
public record AgendaItemDTO(
        String tipo,        // CURSO / DIPLOMADO / ALQUILER / CATERING
        String titulo,
        LocalDate fecha,
        LocalTime horaInicio,   // null en cursos = "todo el día" (rango de fechas)
        LocalTime horaFin,
        String lugar,
        String estado) {
}

