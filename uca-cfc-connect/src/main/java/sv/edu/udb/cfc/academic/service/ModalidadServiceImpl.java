package sv.edu.udb.cfc.academic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.cfc.academic.dto.ModalidadCreateDTO;
import sv.edu.udb.cfc.academic.dto.ModalidadResponseDTO;
import sv.edu.udb.cfc.academic.dto.ModalidadUpdateDTO;
import sv.edu.udb.cfc.academic.entity.Modalidad;
import sv.edu.udb.cfc.academic.repository.CursoRepository;
import sv.edu.udb.cfc.academic.repository.ModalidadRepository;
import sv.edu.udb.cfc.shared.exception.BusinessException;
import sv.edu.udb.cfc.shared.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModalidadServiceImpl implements ModalidadService {

    private final ModalidadRepository modalidadRepository;
    private final CursoRepository cursoRepository;

    @Override
    @Transactional
    public ModalidadResponseDTO crear(ModalidadCreateDTO dto) {
        String nombre = dto.nombre().trim();
        if (modalidadRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BusinessException("Ya existe una modalidad con el nombre: " + nombre);
        }
        Modalidad modalidad = Modalidad.builder()
                .nombre(nombre)
                .descripcion(dto.descripcion())
                .build();
        return ModalidadResponseDTO.from(modalidadRepository.save(modalidad));
    }

    @Override
    @Transactional
    public ModalidadResponseDTO actualizar(Long id, ModalidadUpdateDTO dto) {
        Modalidad modalidad = buscarEntidad(id);
        String nombre = dto.nombre().trim();
        if (modalidadRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new BusinessException("Ya existe una modalidad con el nombre: " + nombre);
        }
        modalidad.setNombre(nombre);
        modalidad.setDescripcion(dto.descripcion());
        if (dto.activo() != null) {
            modalidad.setActivo(dto.activo());
        }
        return ModalidadResponseDTO.from(modalidad);
    }

    @Override
    public ModalidadResponseDTO obtenerPorId(Long id) {
        return ModalidadResponseDTO.from(buscarEntidad(id));
    }

    @Override
    public Page<ModalidadResponseDTO> listar(Boolean activo, Pageable pageable) {
        Page<Modalidad> pagina = Boolean.TRUE.equals(activo)
                ? modalidadRepository.findByActivoTrue(pageable)
                : modalidadRepository.findAll(pageable);
        return pagina.map(ModalidadResponseDTO::from);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        long cursosAsociados = cursoRepository.countByModalidadId(id);
        if (cursosAsociados > 0) {
            throw new BusinessException("No se puede eliminar la modalidad: tiene " + cursosAsociados
                    + " curso(s) asociado(s). Reasigne esos cursos primero.");
        }
        buscarEntidad(id).setActivo(false);
    }

    private Modalidad buscarEntidad(Long id) {
        return modalidadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modalidad", id));
    }
}