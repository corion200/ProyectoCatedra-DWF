package sv.edu.udb.cfc.enrollment.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.cfc.academic.entity.Curso;
import sv.edu.udb.cfc.academic.enums.EstadoCurso;
import sv.edu.udb.cfc.academic.repository.CursoRepository;
import sv.edu.udb.cfc.client.entity.Cliente;
import sv.edu.udb.cfc.client.repository.ClienteRepository;
import sv.edu.udb.cfc.enrollment.dto.InscripcionCreateDTO;
import sv.edu.udb.cfc.enrollment.dto.InscripcionResponseDTO;
import sv.edu.udb.cfc.enrollment.dto.InscripcionUpdateDTO;
import sv.edu.udb.cfc.enrollment.entity.Inscripcion;
import sv.edu.udb.cfc.enrollment.enums.EstadoInscripcion;
import sv.edu.udb.cfc.enrollment.repository.InscripcionRepository;
import sv.edu.udb.cfc.shared.exception.BusinessException;
import sv.edu.udb.cfc.shared.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InscripcionServiceImpl implements InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final ClienteRepository clienteRepository;
    private final CursoRepository cursoRepository;

    /** REGLA: solo estos estados consumen cupo del curso. */
    private static final Set<EstadoInscripcion> ESTADOS_QUE_OCUPAN_CUPO =
            EnumSet.of(EstadoInscripcion.PENDIENTE, EstadoInscripcion.CONFIRMADA);

    @Override
    @Transactional
    public InscripcionResponseDTO inscribir(InscripcionCreateDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", dto.clienteId()));
        Curso curso = cursoRepository.findById(dto.cursoId())
                .orElseThrow(() -> new ResourceNotFoundException("Curso", dto.cursoId()));

        validarClienteInscribible(cliente);
        validarCursoInscribible(curso);

        if (inscripcionRepository.existsByClienteIdAndCursoId(cliente.getId(), curso.getId())) {
            throw new BusinessException("El cliente ya se encuentra inscrito en este curso");
        }

        long inscritos = inscripcionRepository.countByCursoIdAndEstadoIn(curso.getId(), ESTADOS_QUE_OCUPAN_CUPO);
        if (inscritos >= curso.getCupoMaximo()) {
            throw new BusinessException("Cupo agotado para el curso: " + curso.getNombre()
                    + " (" + inscritos + "/" + curso.getCupoMaximo() + ")");
        }

        Inscripcion inscripcion = Inscripcion.builder()
                .codigo(generarCodigo())
                .cliente(cliente)
                .curso(curso)
                .fechaInscripcion(LocalDateTime.now())
                .observaciones(dto.observaciones())
                .build();   // estado nace en PENDIENTE (@Builder.Default)
        return InscripcionResponseDTO.from(inscripcionRepository.save(inscripcion));
    }

    @Override
    @Transactional
    public InscripcionResponseDTO actualizarObservaciones(Long id, InscripcionUpdateDTO dto) {
        Inscripcion inscripcion = buscarEntidad(id);
        inscripcion.setObservaciones(dto.observaciones());
        return InscripcionResponseDTO.from(inscripcion);
    }

    @Override
    @Transactional
    public InscripcionResponseDTO confirmar(Long id) {
        Inscripcion inscripcion = buscarEntidad(id);
        if (inscripcion.getEstado() != EstadoInscripcion.PENDIENTE) {
            throw new BusinessException(
                    "Solo se pueden confirmar inscripciones en estado PENDIENTE (actual: "
                            + inscripcion.getEstado() + ")", HttpStatus.BAD_REQUEST);
        }
        inscripcion.setEstado(EstadoInscripcion.CONFIRMADA);
        return InscripcionResponseDTO.from(inscripcion);
    }

    @Override
    @Transactional
    public InscripcionResponseDTO cancelar(Long id, String motivo) {
        Inscripcion inscripcion = buscarEntidad(id);
        if (inscripcion.getEstado() == EstadoInscripcion.CANCELADA
                || inscripcion.getEstado() == EstadoInscripcion.FINALIZADA) {
            throw new BusinessException("No se puede cancelar una inscripción en estado "
                    + inscripcion.getEstado(), HttpStatus.BAD_REQUEST);
        }
        inscripcion.setEstado(EstadoInscripcion.CANCELADA);
        if (motivo != null && !motivo.isBlank()) {
            inscripcion.setObservaciones(motivo);
        }
        // Al cancelar, la inscripción deja de consumir cupo (solo PENDIENTE/CONFIRMADA cuentan)
        return InscripcionResponseDTO.from(inscripcion);
    }

    @Override
    public InscripcionResponseDTO obtenerPorId(Long id) {
        return InscripcionResponseDTO.from(buscarEntidad(id));
    }

    @Override
    public Page<InscripcionResponseDTO> listar(Long clienteId, Long cursoId,
                                               EstadoInscripcion estado, Pageable pageable) {
        return inscripcionRepository.findAll(conFiltros(clienteId, cursoId, estado), pageable)
                .map(InscripcionResponseDTO::from);
    }

    // ─────────────── reglas de negocio ───────────────

    private void validarClienteInscribible(Cliente cliente) {
        if (!Boolean.TRUE.equals(cliente.getActivo())) {
            throw new BusinessException("El cliente se encuentra inactivo y no puede inscribirse",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private void validarCursoInscribible(Curso curso) {
        if (!Boolean.TRUE.equals(curso.getActivo())) {
            throw new BusinessException("El curso no está activo", HttpStatus.BAD_REQUEST);
        }
        if (curso.getEstado() != EstadoCurso.PROGRAMADO) {
            throw new BusinessException("Solo se permiten inscripciones en cursos PROGRAMADOS (actual: "
                    + curso.getEstado() + ")", HttpStatus.BAD_REQUEST);
        }
    }

    // ─────────────── helpers ───────────────

    private Specification<Inscripcion> conFiltros(Long clienteId, Long cursoId, EstadoInscripcion estado) {
        return (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (clienteId != null) {
                condiciones.add(cb.equal(root.get("cliente").get("id"), clienteId));
            }
            if (cursoId != null) {
                condiciones.add(cb.equal(root.get("curso").get("id"), cursoId));
            }
            if (estado != null) {
                condiciones.add(cb.equal(root.get("estado"), estado));
            }
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
    }

    /**
     * TODO(producción): reemplazar por secuencia de BD o generador dedicado —
     * count()+1 puede colisionar con inscripciones concurrentes.
     */
    private String generarCodigo() {
        return String.format("INS-%d-%05d", LocalDate.now().getYear(),
                inscripcionRepository.count() + 1);
    }

    private Inscripcion buscarEntidad(Long id) {
        return inscripcionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción", id));
    }
}