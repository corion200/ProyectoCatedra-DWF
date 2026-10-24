
package sv.edu.udb.cfc.agenda.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.cfc.agenda.dto.AgendaItemDTO;
import sv.edu.udb.cfc.agenda.service.AgendaService;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST encargado de consultar la agenda institucional
 * del Centro de Formación Continua.
 * Permite obtener las actividades programadas para una fecha específica,
 * incluyendo cursos, alquileres de espacios y servicios de catering.
 */
@Tag(
        name = "Agenda Institucional (Módulo 7)",
        description = "Endpoints para consultar las actividades diarias del centro, "
                + "incluyendo cursos, alquileres y servicios de catering."
)
@RestController
@RequestMapping("/api/v1/agenda")
@RequiredArgsConstructor
public class AgendaController {

    /**
     * Servicio encargado de consultar y organizar las actividades
     * correspondientes a la fecha solicitada.
     */
    private final AgendaService agendaService;

    /**
     * Obtiene las actividades programadas para un día determinado,
     * ordenadas por hora de inicio.
     *
     * @param fecha fecha de consulta en formato ISO (yyyy-MM-dd)
     * @return lista de actividades registradas para la fecha indicada
     */
    @Operation(
            summary = "Consultar la agenda institucional por fecha",
            description = "Devuelve los cursos, alquileres de espacios y servicios "
                    + "de catering programados para la fecha indicada. "
                    + "Las actividades se presentan ordenadas por hora de inicio; "
                    + "las que no tienen una hora definida aparecen al final."
    )
    @GetMapping
    public List<AgendaItemDTO> obtenerAgenda(
            @RequestParam("fecha")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {

        // Delega la consulta y organización de las actividades al servicio.
        return agendaService.obtenerAgenda(fecha);
    }
}
