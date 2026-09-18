package sv.edu.udb.cfc.catering.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.cfc.catering.dto.CateringCreateDTO;
import sv.edu.udb.cfc.catering.dto.CateringResponseDTO;
import sv.edu.udb.cfc.catering.dto.CateringUpdateDTO;
import sv.edu.udb.cfc.catering.entity.CateringServicio;
import sv.edu.udb.cfc.catering.enums.EstadoCatering;
import sv.edu.udb.cfc.catering.repository.CateringServicioRepository;
import sv.edu.udb.cfc.client.entity.Cliente;
import sv.edu.udb.cfc.client.repository.ClienteRepository;
import sv.edu.udb.cfc.quotation.entity.Cotizacion;
import sv.edu.udb.cfc.quotation.enums.EstadoCotizacion;
import sv.edu.udb.cfc.quotation.enums.TipoCotizacion;
import sv.edu.udb.cfc.quotation.repository.CotizacionRepository;
import sv.edu.udb.cfc.shared.exception.BusinessException;
import sv.edu.udb.cfc.shared.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CateringServiceImpl implements CateringService {

    private final CateringServicioRepository cateringRepository;
    private final ClienteRepository clienteRepository;
    private final CotizacionRepository cotizacionRepository;

    @Override
    @Transactional
    public CateringResponseDTO crear(CateringCreateDTO dto) {
        Cliente cliente = validarClienteActivo(dto.clienteId());
        Cotizacion cotizacion = validarCotizacionOrigen(dto.cotizacionId());

        CateringServicio catering = CateringServicio.builder()
                .codigo(generarCodigo())
                .cliente(cliente)
                .cotizacion(cotizacion)
                .tipoServicio(dto.tipoServicio())
                .numeroAsistentes(dto.numeroAsistentes())
                .menu(dto.menu().trim())
                .fechaEvento(dto.fechaEvento())
                .horaEntrega(dto.horaEntrega())
                .lugar(dto.lugar().trim())
                .precioPorPersona(dto.precioPorPersona())
                .costoTotal(calcularCosto(dto.precioPorPersona(), dto.numeroAsistentes()))
                .observaciones(dto.observaciones())
                .build();   // estado nace en PENDIENTE
        return CateringResponseDTO.from(cateringRepository.save(catering));
    }

    @Override
    @Transactional
    public CateringResponseDTO actualizar(Long id, CateringUpdateDTO dto) {
        CateringServicio catering = buscarEntidad(id);
        if (catering.getEstado() == EstadoCatering.CANCELADO
                || catering.getEstado() == EstadoCatering.FINALIZADO) {
            throw new BusinessException("No se puede editar un catering en estado " + catering.getEstado(),
                    HttpStatus.BAD_REQUEST);
        }
        catering.setTipoServicio(dto.tipoServicio());
        catering.setNumeroAsistentes(dto.numeroAsistentes());
        catering.setMenu(dto.menu().trim());
        catering.setFechaEvento(dto.fechaEvento());
        catering.setHoraEntrega(dto.horaEntrega());
        catering.setLugar(dto.lugar().trim());
        catering.setPrecioPorPersona(dto.precioPorPersona());
        // REGLA: el costo SIEMPRE se recalcula si cambian precio o asistentes
        catering.setCostoTotal(calcularCosto(dto.precioPorPersona(), dto.numeroAsistentes()));
        if (dto.observaciones() != null) {
            catering.setObservaciones(dto.observaciones());
        }
        return CateringResponseDTO.from(catering);
    }

    @Override
    public CateringResponseDTO obtenerPorId(Long id) {
        return CateringResponseDTO.from(buscarEntidad(id));
    }

    @Override
    public Page<CateringResponseDTO> listar(Long clienteId, EstadoCatering estado,
                                            LocalDate fecha, Pageable pageable) {
        return cateringRepository.findAll(conFiltros(clienteId, estado, fecha), pageable)
                .map(CateringResponseDTO::from);
    }

    @Override
    @Transactional
    public CateringResponseDTO confirmar(Long id) {
        CateringServicio catering = buscarEntidad(id);
        if (catering.getEstado() != EstadoCatering.PENDIENTE) {
            throw new BusinessException("Solo se confirman caterings en estado PENDIENTE (actual: "
                    + catering.getEstado() + ")", HttpStatus.BAD_REQUEST);
        }
        catering.setEstado(EstadoCatering.CONFIRMADO);
        return CateringResponseDTO.from(catering);
    }

    @Override
    @Transactional
    public CateringResponseDTO finalizar(Long id) {
        CateringServicio catering = buscarEntidad(id);
        if (catering.getEstado() != EstadoCatering.CONFIRMADO) {
            throw new BusinessException("Solo se finalizan caterings en estado CONFIRMADO (actual: "
                    + catering.getEstado() + ")", HttpStatus.BAD_REQUEST);
        }
        catering.setEstado(EstadoCatering.FINALIZADO);
        return CateringResponseDTO.from(catering);
    }

    @Override
    @Transactional
    public CateringResponseDTO cancelar(Long id, String motivo) {
        CateringServicio catering = buscarEntidad(id);
        if (catering.getEstado() == EstadoCatering.CANCELADO
                || catering.getEstado() == EstadoCatering.FINALIZADO) {
            throw new BusinessException("No se puede cancelar un catering en estado " + catering.getEstado(),
                    HttpStatus.BAD_REQUEST);
        }
        catering.setEstado(EstadoCatering.CANCELADO);
        if (motivo != null && !motivo.isBlank()) {
            catering.setObservaciones(motivo);
        }
        return CateringResponseDTO.from(catering);
    }
    @Override
    @Transactional
    public void eliminar(Long id) {
        CateringServicio catering = buscarEntidad(id);
        if (catering.getEstado() != EstadoCatering.PENDIENTE) {
            throw new BusinessException("Solo se pueden eliminar caterings PENDIENTES. "
                    + "Los demás: cancélelos para conservar el historial.", HttpStatus.BAD_REQUEST);
        }
        cateringRepository.delete(catering);
    }

    // ─────────────── helpers ───────────────

    /** costoTotal = precioPorPersona × numeroAsistentes, a 2 decimales. */
    private BigDecimal calcularCosto(BigDecimal precioPorPersona, Integer numeroAsistentes) {
        return precioPorPersona.multiply(BigDecimal.valueOf(numeroAsistentes))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** REGLA: si proviene de cotización, debe estar APROBADA y ser de tipo CATERING. */
    private Cotizacion validarCotizacionOrigen(Long cotizacionId) {
        if (cotizacionId == null) {
            return null;
        }
        Cotizacion cotizacion = cotizacionRepository.findById(cotizacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización", cotizacionId));
        if (cotizacion.getEstado() != EstadoCotizacion.APROBADA) {
            throw new BusinessException("La cotización debe estar APROBADA para generar un servicio "
                    + "de catering (actual: " + cotizacion.getEstado() + ")", HttpStatus.BAD_REQUEST);
        }
        if (cotizacion.getTipo() != TipoCotizacion.CATERING) {
            throw new BusinessException("La cotización es de tipo " + cotizacion.getTipo()
                    + " y no corresponde a CATERING", HttpStatus.BAD_REQUEST);
        }
        return cotizacion;
    }

    private Cliente validarClienteActivo(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteId));
        if (!Boolean.TRUE.equals(cliente.getActivo())) {
            throw new BusinessException("El cliente se encuentra inactivo", HttpStatus.BAD_REQUEST);
        }
        return cliente;
    }

    private Specification<CateringServicio> conFiltros(Long clienteId, EstadoCatering estado,
                                                       LocalDate fecha) {
        return (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (clienteId != null) {
                condiciones.add(cb.equal(root.get("cliente").get("id"), clienteId));
            }
            if (estado != null) {
                condiciones.add(cb.equal(root.get("estado"), estado));
            }
            if (fecha != null) {
                condiciones.add(cb.equal(root.get("fechaEvento"), fecha));
            }
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
    }

    /** TODO(producción): secuencia de BD para evitar colisiones concurrentes. */
    private String generarCodigo() {
        return String.format("CAT-%d-%05d", LocalDate.now().getYear(),
                cateringRepository.count() + 1);
    }

    private CateringServicio buscarEntidad(Long id) {
        return cateringRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio de catering", id));
    }
}