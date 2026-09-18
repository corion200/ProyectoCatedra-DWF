package sv.edu.udb.cfc.quotation.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.cfc.client.entity.Cliente;
import sv.edu.udb.cfc.client.repository.ClienteRepository;
import sv.edu.udb.cfc.quotation.dto.CotizacionCreateDTO;
import sv.edu.udb.cfc.quotation.dto.CotizacionResponseDTO;
import sv.edu.udb.cfc.quotation.dto.CotizacionUpdateDTO;
import sv.edu.udb.cfc.quotation.entity.Cotizacion;
import sv.edu.udb.cfc.quotation.enums.EstadoCotizacion;
import sv.edu.udb.cfc.quotation.enums.TipoCotizacion;
import sv.edu.udb.cfc.quotation.repository.CotizacionRepository;
import sv.edu.udb.cfc.shared.exception.BusinessException;
import sv.edu.udb.cfc.shared.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CotizacionServiceImpl implements CotizacionService {

    private final CotizacionRepository cotizacionRepository;
    private final ClienteRepository clienteRepository;

    private static final Map<EstadoCotizacion, Set<EstadoCotizacion>> TRANSICIONES_VALIDAS = Map.of(
            EstadoCotizacion.PENDIENTE,
            EnumSet.of(EstadoCotizacion.EN_PROCESO, EstadoCotizacion.APROBADA, EstadoCotizacion.RECHAZADA),
            EstadoCotizacion.EN_PROCESO,
            EnumSet.of(EstadoCotizacion.APROBADA, EstadoCotizacion.RECHAZADA),
            EstadoCotizacion.APROBADA, EnumSet.noneOf(EstadoCotizacion.class),
            EstadoCotizacion.RECHAZADA, EnumSet.noneOf(EstadoCotizacion.class));

    @Override
    @Transactional
    public CotizacionResponseDTO crear(CotizacionCreateDTO dto) {
        Cliente cliente = validarClienteActivo(dto.clienteId());
        Cotizacion cotizacion = Cotizacion.builder()
                .codigo(generarCodigo())
                .cliente(cliente)
                .tipo(dto.tipo())
                .descripcion(dto.descripcion().trim())
                .fechaEvento(dto.fechaEvento())
                .montoEstimado(dto.montoEstimado())
                .observaciones(dto.observaciones())
                .build();   // estado nace en PENDIENTE
        return CotizacionResponseDTO.from(cotizacionRepository.save(cotizacion));
    }

    @Override
    @Transactional
    public CotizacionResponseDTO actualizar(Long id, CotizacionUpdateDTO dto) {
        Cotizacion cotizacion = buscarEntidad(id);
        if (cotizacion.getEstado() == EstadoCotizacion.APROBADA
                || cotizacion.getEstado() == EstadoCotizacion.RECHAZADA) {
            throw new BusinessException("No se puede editar una cotización en estado "
                    + cotizacion.getEstado(), HttpStatus.BAD_REQUEST);
        }
        cotizacion.setCliente(validarClienteActivo(dto.clienteId()));
        cotizacion.setTipo(dto.tipo());
        cotizacion.setDescripcion(dto.descripcion().trim());
        cotizacion.setFechaEvento(dto.fechaEvento());
        cotizacion.setMontoEstimado(dto.montoEstimado());
        cotizacion.setObservaciones(dto.observaciones());
        return CotizacionResponseDTO.from(cotizacion);
    }

    @Override
    public CotizacionResponseDTO obtenerPorId(Long id) {
        return CotizacionResponseDTO.from(buscarEntidad(id));
    }

    @Override
    public Page<CotizacionResponseDTO> listar(Long clienteId, TipoCotizacion tipo,
                                              EstadoCotizacion estado, Pageable pageable) {
        return cotizacionRepository.findAll(conFiltros(clienteId, tipo, estado), pageable)
                .map(CotizacionResponseDTO::from);
    }

    @Override
    @Transactional
    public CotizacionResponseDTO cambiarEstado(Long id, EstadoCotizacion nuevoEstado) {
        Cotizacion cotizacion = buscarEntidad(id);
        Set<EstadoCotizacion> permitidos = TRANSICIONES_VALIDAS
                .getOrDefault(cotizacion.getEstado(), EnumSet.noneOf(EstadoCotizacion.class));
        if (!permitidos.contains(nuevoEstado)) {
            throw new BusinessException("Transición de estado inválida: %s → %s. Permitidas: %s"
                    .formatted(cotizacion.getEstado(), nuevoEstado, permitidos), HttpStatus.BAD_REQUEST);
        }
        cotizacion.setEstado(nuevoEstado);
        return CotizacionResponseDTO.from(cotizacion);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Cotizacion cotizacion = buscarEntidad(id);
        // REGLA: solo se eliminan cotizaciones sin compromiso comercial.
        // Las APROBADAS/EN_PROCESO tienen trabajo asociado → se rechazan, no se borran.
        if (cotizacion.getEstado() == EstadoCotizacion.APROBADA
                || cotizacion.getEstado() == EstadoCotizacion.EN_PROCESO) {
            throw new BusinessException("No se puede eliminar una cotización en estado "
                    + cotizacion.getEstado() + ". Recházala primero.", HttpStatus.BAD_REQUEST);
        }
        cotizacionRepository.delete(cotizacion);   // borrado físico (no hay campo activo en esta tabla)
    }

    // ─────────────── helpers ───────────────

    private Specification<Cotizacion> conFiltros(Long clienteId, TipoCotizacion tipo,
                                                 EstadoCotizacion estado) {
        return (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (clienteId != null) {
                condiciones.add(cb.equal(root.get("cliente").get("id"), clienteId));
            }
            if (tipo != null) {
                condiciones.add(cb.equal(root.get("tipo"), tipo));
            }
            if (estado != null) {
                condiciones.add(cb.equal(root.get("estado"), estado));
            }
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
    }

    private Cliente validarClienteActivo(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteId));
        if (!Boolean.TRUE.equals(cliente.getActivo())) {
            throw new BusinessException("El cliente se encuentra inactivo", HttpStatus.BAD_REQUEST);
        }
        return cliente;
    }

    /** TODO(producción): secuencia de BD para evitar colisiones concurrentes. */
    private String generarCodigo() {
        return String.format("COT-%d-%05d", LocalDate.now().getYear(),
                cotizacionRepository.count() + 1);
    }

    private Cotizacion buscarEntidad(Long id) {
        return cotizacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización", id));
    }
}