package sv.edu.udb.cfc.academic.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.cfc.academic.dto.CursoCreateDTO;
import sv.edu.udb.cfc.academic.dto.CursoResponseDTO;
import sv.edu.udb.cfc.academic.dto.CursoUpdateDTO;
import sv.edu.udb.cfc.academic.entity.Categoria;
import sv.edu.udb.cfc.academic.entity.Curso;
import sv.edu.udb.cfc.academic.entity.Docente;
import sv.edu.udb.cfc.academic.entity.Modalidad;
import sv.edu.udb.cfc.academic.enums.EstadoCurso;
import sv.edu.udb.cfc.academic.enums.TipoOferta;
import sv.edu.udb.cfc.academic.repository.CategoriaRepository;
import sv.edu.udb.cfc.academic.repository.CursoRepository;
import sv.edu.udb.cfc.academic.repository.DocenteRepository;
import sv.edu.udb.cfc.academic.repository.ModalidadRepository;
import sv.edu.udb.cfc.shared.exception.BusinessException;
import sv.edu.udb.cfc.shared.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CursoServiceImpl implements CursoService {

    private final CursoRepository cursoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ModalidadRepository modalidadRepository;
    private final DocenteRepository docenteRepository;

    /** Transiciones permitidas: los estados terminales (FINALIZADO, CANCELADO) no tienen salida. */
    private static final Map<EstadoCurso, Set<EstadoCurso>> TRANSICIONES_VALIDAS = Map.of(
            EstadoCurso.PROGRAMADO, EnumSet.of(EstadoCurso.EN_CURSO, EstadoCurso.CANCELADO),
            EstadoCurso.EN_CURSO, EnumSet.of(EstadoCurso.FINALIZADO, EstadoCurso.CANCELADO),
            EstadoCurso.FINALIZADO, EnumSet.noneOf(EstadoCurso.class),
            EstadoCurso.CANCELADO, EnumSet.noneOf(EstadoCurso.class));

    @Override
    @Transactional
    public CursoResponseDTO crear(CursoCreateDTO dto) {
        String codigo = dto.codigo().trim().toUpperCase();
        validarCodigoUnico(codigo, null);
        validarRangoFechas(dto.fechaInicio(), dto.fechaFin());

        Curso curso = Curso.builder()
                .codigo(codigo)
                .nombre(dto.nombre().trim())
                .descripcion(dto.descripcion())
                .tipo(dto.tipo())
                .categoria(buscarCategoria(dto.categoriaId()))
                .modalidad(buscarModalidad(dto.modalidadId()))
                .horas(dto.horas())
                .precio(dto.precio())
                .cupoMaximo(dto.cupoMaximo())
                .fechaInicio(dto.fechaInicio())
                .fechaFin(dto.fechaFin())
                .docentes(cargarDocentes(dto.docentesIds()))
                .build();   // estado nace en PROGRAMADO (@Builder.Default)
        return CursoResponseDTO.from(cursoRepository.save(curso));
    }

    @Override
    @Transactional
    public CursoResponseDTO actualizar(Long id, CursoUpdateDTO dto) {
        Curso curso = buscarEntidad(id);
        String codigo = dto.codigo().trim().toUpperCase();
        validarCodigoUnico(codigo, id);
        validarRangoFechas(dto.fechaInicio(), dto.fechaFin());

        curso.setCodigo(codigo);
        curso.setNombre(dto.nombre().trim());
        curso.setDescripcion(dto.descripcion());
        curso.setTipo(dto.tipo());
        curso.setCategoria(buscarCategoria(dto.categoriaId()));
        curso.setModalidad(buscarModalidad(dto.modalidadId()));
        curso.setHoras(dto.horas());
        curso.setPrecio(dto.precio());
        curso.setCupoMaximo(dto.cupoMaximo());
        curso.setFechaInicio(dto.fechaInicio());
        curso.setFechaFin(dto.fechaFin());
        // Reemplazo completo de la relación N:M con docentes
        curso.getDocentes().clear();
        curso.getDocentes().addAll(cargarDocentes(dto.docentesIds()));
        if (dto.activo() != null) {
            curso.setActivo(dto.activo());
        }
        return CursoResponseDTO.from(curso);
    }

    @Override
    public CursoResponseDTO obtenerPorId(Long id) {
        return CursoResponseDTO.from(buscarEntidad(id));
    }

    @Override
    public Page<CursoResponseDTO> listar(String nombre, TipoOferta tipo, Long categoriaId,
                                         Long modalidadId, EstadoCurso estado, Pageable pageable) {
        return cursoRepository
                .findAll(conFiltros(nombre, tipo, categoriaId, modalidadId, estado), pageable)
                .map(CursoResponseDTO::from);
    }

    @Override
    @Transactional
    public CursoResponseDTO cambiarEstado(Long id, EstadoCurso nuevoEstado) {
        Curso curso = buscarEntidad(id);
        Set<EstadoCurso> permitidos = TRANSICIONES_VALIDAS
                .getOrDefault(curso.getEstado(), EnumSet.noneOf(EstadoCurso.class));
        if (!permitidos.contains(nuevoEstado)) {
            throw new BusinessException(
                    "Transición de estado inválida: %s → %s. Transiciones permitidas desde %s: %s"
                            .formatted(curso.getEstado(), nuevoEstado, curso.getEstado(), permitidos),
                    HttpStatus.BAD_REQUEST);
        }
        curso.setEstado(nuevoEstado);
        return CursoResponseDTO.from(curso);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        // Soft delete: las inscripciones y pagos históricos deben conservar su referencia al curso
        buscarEntidad(id).setActivo(false);
    }

    // ─────────────── helpers ───────────────

    /** Specification con filtros dinámicos combinables (solo agrega condiciones de los presentes). */
    private Specification<Curso> conFiltros(String nombre, TipoOferta tipo,
                                            Long categoriaId, Long modalidadId, EstadoCurso estado) {
        return (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (nombre != null && !nombre.isBlank()) {
                condiciones.add(cb.like(cb.lower(root.get("nombre")),
                        "%" + nombre.trim().toLowerCase() + "%"));
            }
            if (tipo != null) {
                condiciones.add(cb.equal(root.get("tipo"), tipo));
            }
            if (categoriaId != null) {
                condiciones.add(cb.equal(root.get("categoria").get("id"), categoriaId));
            }
            if (modalidadId != null) {
                condiciones.add(cb.equal(root.get("modalidad").get("id"), modalidadId));
            }
            if (estado != null) {
                condiciones.add(cb.equal(root.get("estado"), estado));
            }
            condiciones.add(cb.isTrue(root.get("activo")));   // siempre ocultar eliminados
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
    }

    private Curso buscarEntidad(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso", id));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", id));
    }

    private Modalidad buscarModalidad(Long id) {
        return modalidadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modalidad", id));
    }

    /** Carga los docentes por id y verifica que existan TODOS y estén activos. */
    private Set<Docente> cargarDocentes(Set<Long> ids) {
        Set<Docente> docentes = new HashSet<>(docenteRepository.findAllById(ids));
        if (docentes.size() != ids.size()) {
            throw new ResourceNotFoundException("Uno o más docentes indicados no existen");
        }
        docentes.forEach(d -> {
            if (!Boolean.TRUE.equals(d.getActivo())) {
                throw new BusinessException("El docente " + d.getNombres() + " " + d.getApellidos()
                        + " está inactivo y no puede asignarse al curso");
            }
        });
        return docentes;
    }

    private void validarCodigoUnico(String codigo, Long idExcluido) {
        boolean existe = (idExcluido == null)
                ? cursoRepository.existsByCodigoIgnoreCase(codigo)
                : cursoRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, idExcluido);
        if (existe) {
            throw new BusinessException("Ya existe un curso con el código: " + codigo);
        }
    }

    private void validarRangoFechas(LocalDate inicio, LocalDate fin) {
        if (fin.isBefore(inicio)) {
            throw new BusinessException("La fecha de fin no puede ser anterior a la fecha de inicio",
                    HttpStatus.BAD_REQUEST);
        }
    }
}