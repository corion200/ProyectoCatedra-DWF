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

// Servicio que junta las actividades del centro en una fecha
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AgendaService {

    private final CursoRepository cursoRepository;
    private final AlquilerRepository alquilerRepository;
    private final CateringServicioRepository cateringRepository;

    // Arma la agenda completa de un día
    public List<AgendaItemDTO> obtenerAgenda(LocalDate fecha) {
        List<AgendaItemDTO> items = new ArrayList<>();

        // Filtro de cursos activos que cubren la fecha
        Specification<Curso> filtroCursos = (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            condiciones.add(cb.lessThanOrEqualTo(root.get("fechaInicio"), fecha));
            condiciones.add(cb.greaterThanOrEqualTo(root.get("fechaFin"), fecha));
            condiciones.add(cb.isTrue(root.get("activo")));
            condiciones.add(cb.notEqual(root.get("estado"), EstadoCurso.CANCELADO));
            return cb.and(condiciones.toArray(new Predicate[0]));
        };

        // Agrega los cursos
        cursoRepository.findAll(filtroCursos).forEach(c -> items.add(new AgendaItemDTO(
                c.getTipo().name(), c.getNombre(), fecha, null, null,
                "Modalidad: " + c.getModalidad().getNombre(), c.getEstado().name())));

        // Agrega los alquileres no cancelados
        alquilerRepository.findByFechaEventoAndEstadoNot(fecha, EstadoAlquiler.CANCELADA)
                .forEach(a -> items.add(new AgendaItemDTO(
                        "ALQUILER", a.getEspacio().getNombre() + " — " + a.getCliente().getNombre(),
                        fecha, a.getHoraInicio(), a.getHoraFin(),
                        a.getEspacio().getNombre(), a.getEstado().name())));

        // Agrega los servicios de catering no cancelados
        cateringRepository.findByFechaEventoAndEstadoNot(fecha, EstadoCatering.CANCELADO)
                .forEach(c -> items.add(new AgendaItemDTO(
                        "CATERING", c.getTipoServicio().name() + " — " + c.getCliente().getNombre(),
                        fecha, c.getHoraEntrega(), null, c.getLugar(), c.getEstado().name())));

        // Ordena por hora, los que no tienen hora van al final
        items.sort(Comparator.comparing(AgendaItemDTO::horaInicio,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return items;
    }
}