package sv.edu.udb.cfc.agenda.dto;

import java.time.LocalDate;
import java.time.LocalTime;

// Datos de una actividad en la agenda
public record AgendaItemDTO(
        String tipo,            // Curso, diplomado, alquiler o catering
        String titulo,          // Nombre de la actividad
        LocalDate fecha,        // Día de la actividad
        LocalTime horaInicio,   // Si es null, dura
        LocalTime horaFin,      // Hora en que termina
        String lugar,           // Sitio donde se realiza
        String estado) {        // Estado actual de la actividad
}