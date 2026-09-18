package sv.edu.udb.cfc.academic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.cfc.academic.dto.DocenteCreateDTO;
import sv.edu.udb.cfc.academic.dto.DocenteResponseDTO;
import sv.edu.udb.cfc.academic.dto.DocenteUpdateDTO;
import sv.edu.udb.cfc.academic.entity.Docente;
import sv.edu.udb.cfc.academic.repository.DocenteRepository;
import sv.edu.udb.cfc.shared.exception.BusinessException;
import sv.edu.udb.cfc.shared.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocenteServiceImpl implements DocenteService {

    private final DocenteRepository docenteRepository;

    @Override
    @Transactional
    public DocenteResponseDTO crear(DocenteCreateDTO dto) {
        validarDuiUnico(dto.dui(), null);
        validarCorreoUnico(dto.correo(), null);

        Docente docente = Docente.builder()
                .nombres(dto.nombres().trim())
                .apellidos(dto.apellidos().trim())
                .dui(dto.dui().trim())
                .nit(normalizarOpcional(dto.nit()))   // vacío → null (evita chocar con UNIQUE de BD)
                .correo(dto.correo().trim().toLowerCase())
                .telefono(normalizarOpcional(dto.telefono()))
                .tituloProfesional(dto.tituloProfesional())
                .especialidad(dto.especialidad())
                .build();
        return DocenteResponseDTO.from(docenteRepository.save(docente));
    }

    @Override
    @Transactional
    public DocenteResponseDTO actualizar(Long id, DocenteUpdateDTO dto) {
        Docente docente = buscarEntidad(id);
        validarDuiUnico(dto.dui(), id);
        validarCorreoUnico(dto.correo(), id);

        docente.setNombres(dto.nombres().trim());
        docente.setApellidos(dto.apellidos().trim());
        docente.setDui(dto.dui().trim());
        docente.setNit(normalizarOpcional(dto.nit()));
        docente.setCorreo(dto.correo().trim().toLowerCase());
        docente.setTelefono(normalizarOpcional(dto.telefono()));
        docente.setTituloProfesional(dto.tituloProfesional());
        docente.setEspecialidad(dto.especialidad());
        if (dto.activo() != null) {
            docente.setActivo(dto.activo());
        }
        return DocenteResponseDTO.from(docente);
    }

    @Override
    public DocenteResponseDTO obtenerPorId(Long id) {
        return DocenteResponseDTO.from(buscarEntidad(id));
    }

    @Override
    public Page<DocenteResponseDTO> listar(String busqueda, Boolean activo, Pageable pageable) {
        Page<Docente> pagina;
        if (busqueda != null && !busqueda.isBlank()) {
            String texto = busqueda.trim();
            pagina = docenteRepository
                    .findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(texto, texto, pageable);
        } else if (Boolean.TRUE.equals(activo)) {
            pagina = docenteRepository.findByActivoTrue(pageable);
        } else {
            pagina = docenteRepository.findAll(pageable);
        }
        return pagina.map(DocenteResponseDTO::from);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        // Soft delete sin validación previa: los cursos históricos conservan la
        // relación N:M (curso_docente) y el docente solo deja de estar disponible.
        buscarEntidad(id).setActivo(false);
    }

    // ─────────────── helpers ───────────────

    private void validarDuiUnico(String dui, Long idExcluido) {
        boolean existe = (idExcluido == null)
                ? docenteRepository.existsByDui(dui.trim())
                : docenteRepository.existsByDuiAndIdNot(dui.trim(), idExcluido);
        if (existe) {
            throw new BusinessException("Ya existe un docente con el DUI: " + dui);
        }
    }

    private void validarCorreoUnico(String correo, Long idExcluido) {
        String normalizado = correo.trim().toLowerCase();
        boolean existe = (idExcluido == null)
                ? docenteRepository.existsByCorreoIgnoreCase(normalizado)
                : docenteRepository.existsByCorreoIgnoreCaseAndIdNot(normalizado, idExcluido);
        if (existe) {
            throw new BusinessException("Ya existe un docente con el correo: " + correo);
        }
    }

    /** Convierte cadenas vacías/espacios en null para campos únicos opcionales. */
    private String normalizarOpcional(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    private Docente buscarEntidad(Long id) {
        return docenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Docente", id));
    }
}