package sv.edu.udb.cfc.venue.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.cfc.venue.dto.EspacioCreateDTO;
import sv.edu.udb.cfc.venue.dto.EspacioResponseDTO;
import sv.edu.udb.cfc.venue.dto.EspacioUpdateDTO;
import sv.edu.udb.cfc.venue.entity.Espacio;
import sv.edu.udb.cfc.venue.enums.TipoEspacio;
import sv.edu.udb.cfc.venue.repository.EspacioRepository;
import sv.edu.udb.cfc.shared.exception.BusinessException;
import sv.edu.udb.cfc.shared.exception.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EspacioServiceImpl implements EspacioService {

    private final EspacioRepository espacioRepository;

    @Override
    @Transactional
    public EspacioResponseDTO crear(EspacioCreateDTO dto) {
        String codigo = dto.codigo().trim().toUpperCase();
        if (espacioRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new BusinessException("Ya existe un espacio con el código: " + codigo);
        }
        Espacio espacio = Espacio.builder()
                .codigo(codigo)
                .nombre(dto.nombre().trim())
                .tipo(dto.tipo())
                .capacidad(dto.capacidad())
                .precioHora(dto.precioHora())
                .equipamiento(dto.equipamiento())
                .disponible(dto.disponible() != null ? dto.disponible() : true)
                .build();
        return EspacioResponseDTO.from(espacioRepository.save(espacio));
    }

    @Override
    @Transactional
    public EspacioResponseDTO actualizar(Long id, EspacioUpdateDTO dto) {
        Espacio espacio = buscarEntidad(id);
        String codigo = dto.codigo().trim().toUpperCase();
        if (espacioRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new BusinessException("Ya existe un espacio con el código: " + codigo);
        }
        espacio.setCodigo(codigo);
        espacio.setNombre(dto.nombre().trim());
        espacio.setTipo(dto.tipo());
        espacio.setCapacidad(dto.capacidad());
        espacio.setPrecioHora(dto.precioHora());
        espacio.setEquipamiento(dto.equipamiento());
        if (dto.disponible() != null) {
            espacio.setDisponible(dto.disponible());
        }
        if (dto.activo() != null) {
            espacio.setActivo(dto.activo());
        }
        return EspacioResponseDTO.from(espacio);
    }

    @Override
    public EspacioResponseDTO obtenerPorId(Long id) {
        return EspacioResponseDTO.from(buscarEntidad(id));
    }

    @Override
    public Page<EspacioResponseDTO> listar(String nombre, TipoEspacio tipo, Integer capacidadMinima,
                                           Boolean disponible, Pageable pageable) {
        return espacioRepository.findAll(conFiltros(nombre, tipo, capacidadMinima, disponible), pageable)
                .map(EspacioResponseDTO::from);
    }

    @Override
    public List<EspacioResponseDTO> listarDisponibles() {
        return espacioRepository.findByDisponibleTrueAndActivoTrue().stream()
                .map(EspacioResponseDTO::from)
                .toList();
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        // Soft delete: los alquileres históricos conservan la referencia al espacio
        Espacio espacio = buscarEntidad(id);
        espacio.setActivo(false);
        espacio.setDisponible(false);
    }

    // ─────────────── helpers ───────────────

    private Specification<Espacio> conFiltros(String nombre, TipoEspacio tipo,
                                              Integer capacidadMinima, Boolean disponible) {
        return (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (nombre != null && !nombre.isBlank()) {
                condiciones.add(cb.like(cb.lower(root.get("nombre")),
                        "%" + nombre.trim().toLowerCase() + "%"));
            }
            if (tipo != null) {
                condiciones.add(cb.equal(root.get("tipo"), tipo));
            }
            if (capacidadMinima != null) {
                condiciones.add(cb.greaterThanOrEqualTo(root.get("capacidad"), capacidadMinima));
            }
            if (disponible != null) {
                condiciones.add(cb.equal(root.get("disponible"), disponible));
            }
            condiciones.add(cb.isTrue(root.get("activo")));
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
    }

    private Espacio buscarEntidad(Long id) {
        return espacioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Espacio", id));
    }
}