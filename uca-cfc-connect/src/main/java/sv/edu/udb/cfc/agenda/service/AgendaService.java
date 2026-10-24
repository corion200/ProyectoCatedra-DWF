
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

// Servicio encargado de reunir las actividades programadas para una fecha.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AgendaService {

    private final CursoRepository cursoRepository;
    private final AlquilerRepository alquilerRepository;
    private final CateringServicioRepository cateringRepository;

    // Consulta y organiza las actividades correspondientes al día indicado.
    public List<AgendaItemDTO> obtenerAgenda(LocalDate fecha) {
        List<AgendaItemDTO> items = new ArrayList<>();

        // Busca cursos activos que incluyan la fecha seleccionada.
        Specification<Curso> filtroCursos = (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            condiciones.add(cb.lessThanOrEqualTo(root.get("fechaInicio"), fecha));
            condiciones.add(cb.greaterThanOrEqualTo(root.get("fechaFin"), fecha));
            condiciones.add(cb.isTrue(root.get("activo")));
            condiciones.add(cb.notEqual(root.get("estado"), EstadoCurso.CANCELADO));
            return cb.and(condiciones.toArray(new Predicate[0]));
        };

        // Incorpora los cursos encontrados a la lista de actividades.
        cursoRepository.findAll(filtroCursos).forEach(c -> items.add(new AgendaItemDTO(
                c.getTipo().name(), c.getNombre(), fecha, null, null,
                "Modalidad: " + c.getModalidad().getNombre(), c.getEstado().name())));

        // Incluye los alquileres cuyo estado no sea cancelado.
        alquilerRepository.findByFechaEventoAndEstadoNot(fecha, EstadoAlquiler.CANCELADA)
                .forEach(a -> items.add(new AgendaItemDTO(
                        "ALQUILER", a.getEspacio().getNombre() + " — " + a.getCliente().getNombre(),
                        fecha, a.getHoraInicio(), a.getHoraFin(),
                        a.getEspacio().getNombre(), a.getEstado().name())));

        // Añade los servicios de catering que no estén cancelados.
        cateringRepository.findByFechaEventoAndEstadoNot(fecha, EstadoCatering.CANCELADO)
                .forEach(c -> items.add(new AgendaItemDTO(
                        "CATERING", c.getTipoServicio().name() + " — " + c.getCliente().getNombre(),
                        fecha, c.getHoraEntrega(), null, c.getLugar(), c.getEstado().name())));

        // Organiza las actividades por hora y deja al final las que no tienen horario.
        items.sort(Comparator.comparing(AgendaItemDTO::horaInicio,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return items;
    }
}
