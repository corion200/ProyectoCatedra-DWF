package sv.edu.udb.cfc.academic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.cfc.academic.dto.CategoriaCreateDTO;
import sv.edu.udb.cfc.academic.dto.CategoriaResponseDTO;
import sv.edu.udb.cfc.academic.dto.CategoriaUpdateDTO;
import sv.edu.udb.cfc.academic.entity.Categoria;
import sv.edu.udb.cfc.academic.repository.CategoriaRepository;
import sv.edu.udb.cfc.academic.repository.CursoRepository;
import sv.edu.udb.cfc.shared.exception.BusinessException;
import sv.edu.udb.cfc.shared.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)   // Por defecto solo lectura; los escritura llevan @Transactional propio
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CursoRepository cursoRepository;

    @Override
    @Transactional
    public CategoriaResponseDTO crear(CategoriaCreateDTO dto) {
        String nombre = dto.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BusinessException("Ya existe una categoría con el nombre: " + nombre);
        }
        Categoria categoria = Categoria.builder()
                .nombre(nombre)
                .descripcion(dto.descripcion())
                .build();
        return CategoriaResponseDTO.from(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional
    public CategoriaResponseDTO actualizar(Long id, CategoriaUpdateDTO dto) {
        Categoria categoria = buscarEntidad(id);
        String nombre = dto.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new BusinessException("Ya existe una categoría con el nombre: " + nombre);
        }
        categoria.setNombre(nombre);
        categoria.setDescripcion(dto.descripcion());
        if (dto.activo() != null) {
            categoria.setActivo(dto.activo());
        }
        // Sin save(): Hibernate detecta el cambio (dirty checking) y genera el UPDATE al cerrar la transacción
        return CategoriaResponseDTO.from(categoria);
    }

    @Override
    public CategoriaResponseDTO obtenerPorId(Long id) {
        return CategoriaResponseDTO.from(buscarEntidad(id));
    }

    @Override
    public Page<CategoriaResponseDTO> listar(String nombre, Boolean activo, Pageable pageable) {
        Page<Categoria> pagina;
        if (nombre != null && !nombre.isBlank()) {
            pagina = categoriaRepository.findByNombreContainingIgnoreCase(nombre.trim(), pageable);
        } else if (Boolean.TRUE.equals(activo)) {
            pagina = categoriaRepository.findByActivoTrue(pageable);
        } else {
            pagina = categoriaRepository.findAll(pageable);
        }
        return pagina.map(CategoriaResponseDTO::from);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        long cursosAsociados = cursoRepository.countByCategoriaId(id);
        if (cursosAsociados > 0) {
            throw new BusinessException("No se puede eliminar la categoría: tiene " + cursosAsociados
                    + " curso(s) asociado(s). Reasigne esos cursos primero.");
        }
        buscarEntidad(id).setActivo(false);   // Soft delete: conserva historial
    }

    private Categoria buscarEntidad(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", id));
    }
}