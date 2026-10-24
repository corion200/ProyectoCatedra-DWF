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

// Controlador de la agenda del centro
@Tag(name = "Agenda Institucional (Módulo 7)", description = "Agenda del centro con cursos, alquileres y catering de un día")
@RestController
@RequestMapping("/api/v1/agenda")
@RequiredArgsConstructor
public class AgendaController {

    // Servicio que arma la agenda
    private final AgendaService agendaService;

    // Devuelve las actividades de un día ordenadas por hora
    @Operation(summary = "Ver la agenda de un día, ordenada por hora")
    @GetMapping
    public List<AgendaItemDTO> obtenerAgenda(
            // Fecha a consultar (formato yyyy-MM-dd)
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        // Pide los datos al servicio
        return agendaService.obtenerAgenda(fecha);
    }
}