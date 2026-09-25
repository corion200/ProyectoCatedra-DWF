package sv.edu.udb.cfc.agenda.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.cfc.academic.entity.Curso;
import sv.edu.udb.cfc.academic.enums.EstadoCurso;
import sv.edu.udb.cfc.academic.repository.CursoRepository;
import sv.edu.udb.cfc.agenda.dto.AgendaItemDTO;
import sv.edu.udb.cfc.catering.enums.EstadoCatering;
import sv.edu.udb.cfc.catering.repository.CateringServicioRepository;
import sv.edu.udb.cfc.venue.enums.EstadoAlquiler;
import sv.edu.udb.cfc.venue.repository.AlquilerRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Módulo 7 — Agenda Institucional (RF13): consolida TODAS las actividades
 * del centro en una fecha (cursos en curso, alquileres y catering).
 * La regla del enunciado "no podrán existir conflictos de horario" ya la
 * garantiza el anti-solapamiento del módulo de alquileres.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AgendaService {

    private final CursoRepository cursoRepository;
    private final AlquilerRepository alquilerRepository;
    private final CateringServicioRepository cateringRepository;

    public List<AgendaItemDTO> obtenerAgenda(LocalDate fecha) {
        List<AgendaItemDTO> items = new ArrayList<>();

        Specification<Curso> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.lessThanOrEqualTo(root.get("fechaInicio"), fecha));
            p.add(cb.greaterThanOrEqualTo(root.get("fechaFin"), fecha));
            p.add(cb.isTrue(root.get("activo")));
            p.add(cb.notEqual(root.get("estado"), EstadoCurso.CANCELADO));
            return cb.and(p.toArray(new Predicate[0]));
        };
        cursoRepository.findAll(spec).forEach(c -> items.add(new AgendaItemDTO(
                c.getTipo().name(), c.getNombre(), fecha, null, null,
                "Modalidad: " + c.getModalidad().getNombre(), c.getEstado().name())));

        alquilerRepository.findByFechaEventoAndEstadoNot(fecha, EstadoAlquiler.CANCELADA)
                .forEach(a -> items.add(new AgendaItemDTO(
                        "ALQUILER", a.getEspacio().getNombre() + " — " + a.getCliente().getNombre(),
                        fecha, a.getHoraInicio(), a.getHoraFin(),
                        a.getEspacio().getNombre(), a.getEstado().name())));

        cateringRepository.findByFechaEventoAndEstadoNot(fecha, EstadoCatering.CANCELADO)
                .forEach(c -> items.add(new AgendaItemDTO(
                        "CATERING", c.getTipoServicio().name() + " — " + c.getCliente().getNombre(),
                        fecha, c.getHoraEntrega(), null, c.getLugar(), c.getEstado().name())));

        items.sort(Comparator.comparing(AgendaItemDTO::horaInicio,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return items;
    }
}

