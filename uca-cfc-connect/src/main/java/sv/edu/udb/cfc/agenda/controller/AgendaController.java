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

@Tag(name = "Agenda Institucional (Módulo 7)", description = "Calendario consolidado: cursos, alquileres y catering por fecha")
@RestController
@RequestMapping("/api/v1/agenda")
@RequiredArgsConstructor
public class AgendaController {

    private final AgendaService agendaService;

    @Operation(summary = "Agenda de una fecha: todas las actividades del centro ordenadas por hora")
    @GetMapping
    public List<AgendaItemDTO> obtenerAgenda(

            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return agendaService.obtenerAgenda(fecha);
    }
}
