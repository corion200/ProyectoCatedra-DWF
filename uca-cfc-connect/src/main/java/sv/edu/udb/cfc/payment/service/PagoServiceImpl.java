package sv.edu.udb.cfc.payment.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.cfc.academic.entity.Curso;
import sv.edu.udb.cfc.catering.entity.CateringServicio;
import sv.edu.udb.cfc.catering.enums.EstadoCatering;
import sv.edu.udb.cfc.catering.repository.CateringServicioRepository;
import sv.edu.udb.cfc.client.entity.Cliente;
import sv.edu.udb.cfc.enrollment.entity.Inscripcion;
import sv.edu.udb.cfc.enrollment.enums.EstadoInscripcion;
import sv.edu.udb.cfc.enrollment.repository.InscripcionRepository;
import sv.edu.udb.cfc.payment.dto.PagoCreateDTO;
import sv.edu.udb.cfc.payment.dto.PagoResponseDTO;
import sv.edu.udb.cfc.payment.dto.ResumenPagoDTO;
import sv.edu.udb.cfc.payment.entity.Pago;
import sv.edu.udb.cfc.payment.enums.EstadoPago;
import sv.edu.udb.cfc.payment.enums.MetodoPago;
import sv.edu.udb.cfc.payment.repository.PagoRepository;
import sv.edu.udb.cfc.quotation.entity.Cotizacion;
import sv.edu.udb.cfc.quotation.enums.EstadoCotizacion;
import sv.edu.udb.cfc.quotation.repository.CotizacionRepository;
import sv.edu.udb.cfc.shared.exception.BusinessException;
import sv.edu.udb.cfc.shared.exception.ResourceNotFoundException;
import sv.edu.udb.cfc.venue.entity.Alquiler;
import sv.edu.udb.cfc.venue.enums.EstadoAlquiler;
import sv.edu.udb.cfc.venue.repository.AlquilerRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final CotizacionRepository cotizacionRepository;
    private final AlquilerRepository alquilerRepository;
    private final CateringServicioRepository cateringRepository;

    /** Estados cuyo monto computa como abono al documento. */
    private static final Set<EstadoPago> ESTADOS_APLICABLES =
            EnumSet.of(EstadoPago.PARCIAL, EstadoPago.PAGADO);

    @Override
    @Transactional
    public PagoResponseDTO registrar(PagoCreateDTO dto) {
        Pago.PagoBuilder builder = Pago.builder()
                .codigo(generarNumeroRecibo())
                .monto(dto.monto())
                .metodoPago(dto.metodoPago())
                .numeroComprobante(normalizarOpcional(dto.numeroComprobante()))
                .observaciones(dto.observaciones())
                .fechaPago(LocalDateTime.now());

        // REGLA: exactamente UNA referencia de las 4 (coherente con CHECK ck_pago_documento)
        int referencias = contarReferencias(dto);
        if (referencias == 0) {
            throw new BusinessException("El pago debe asociarse a exactamente un documento: "
                    + "inscripción, cotización, alquiler o catering", HttpStatus.BAD_REQUEST);
        }
        if (referencias > 1) {
            throw new BusinessException("El pago solo puede asociarse a UN documento, se recibieron "
                    + referencias + " referencias", HttpStatus.BAD_REQUEST);
        }

        if (dto.inscripcionId() != null) {
            builder.inscripcion(validarInscripcionPagable(dto.inscripcionId()));
        } else if (dto.cotizacionId() != null) {
            builder.cotizacion(validarCotizacionPagable(dto.cotizacionId()));
        } else if (dto.alquilerId() != null) {
            builder.alquiler(validarAlquilerPagable(dto.alquilerId()));
        } else {
            builder.catering(validarCateringPagable(dto.cateringId()));
        }

        return PagoResponseDTO.from(pagoRepository.save(builder.build()));
    }

    @Override
    public PagoResponseDTO obtenerPorId(Long id) {
        return PagoResponseDTO.from(buscarEntidad(id));
    }

    @Override
    public Page<PagoResponseDTO> listar(EstadoPago estado, MetodoPago metodoPago,
                                        Long inscripcionId, Pageable pageable) {
        return pagoRepository.findAll(conFiltros(estado, metodoPago, inscripcionId), pageable)
                .map(PagoResponseDTO::from);
    }

    @Override
    @Transactional
    public PagoResponseDTO cambiarEstado(Long id, EstadoPago nuevoEstado) {
        Pago pago = buscarEntidad(id);
        boolean transicionValida = switch (nuevoEstado) {   // Java 21 switch expression
            case PARCIAL   -> pago.getEstado() == EstadoPago.PENDIENTE;
            case PAGADO    -> pago.getEstado() == EstadoPago.PENDIENTE
                    || pago.getEstado() == EstadoPago.PARCIAL;
            case PENDIENTE -> false;   // nunca se regresa a pendiente
        };
        if (!transicionValida) {
            throw new BusinessException("Transición de estado de pago inválida: "
                    + pago.getEstado() + " → " + nuevoEstado, HttpStatus.BAD_REQUEST);
        }
        pago.setEstado(nuevoEstado);
        return PagoResponseDTO.from(pago);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Pago pago = buscarEntidad(id);
        if (pago.getEstado() == EstadoPago.PAGADO) {
            throw new BusinessException("No es posible eliminar un pago liquidado (PAGADO). "
                    + "Emita una reversa o nota de crédito.", HttpStatus.BAD_REQUEST);
        }
        pagoRepository.delete(pago);   // borrado físico: no hay dato histórico que referencie
    }

    @Override
    public ResumenPagoDTO resumenInscripcion(Long inscripcionId) {
        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción", inscripcionId));
        Curso curso = inscripcion.getCurso();
        BigDecimal precio = curso.getPrecio();
        BigDecimal pagado = pagoRepository.sumarMontosPorInscripcion(inscripcionId, ESTADOS_APLICABLES);
        BigDecimal saldo = precio.subtract(pagado).max(BigDecimal.ZERO);
        return new ResumenPagoDTO(inscripcionId, precio, pagado, saldo);
    }

    // ─────────────── validaciones del documento destino ───────────────

    /** REGLA: no se paga una inscripción CANCELADA (PENDIENTE/CONFIRMADA/FINALIZADA sí). */
    private Inscripcion validarInscripcionPagable(Long id) {
        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción", id));
        if (inscripcion.getEstado() == EstadoInscripcion.CANCELADA) {
            throw new BusinessException("No se pueden registrar pagos sobre una inscripción CANCELADA",
                    HttpStatus.BAD_REQUEST);
        }
        return inscripcion;
    }

    /** REGLA: no se paga una cotización RECHAZADA (un anticipo sobre PENDIENTE/EN_PROCESO es válido). */
    private Cotizacion validarCotizacionPagable(Long id) {
        Cotizacion cotizacion = cotizacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización", id));
        if (cotizacion.getEstado() == EstadoCotizacion.RECHAZADA) {
            throw new BusinessException("No se pueden registrar pagos sobre una cotización RECHAZADA",
                    HttpStatus.BAD_REQUEST);
        }
        return cotizacion;
    }

    /** REGLA: no se paga un alquiler CANCELADO. */
    private Alquiler validarAlquilerPagable(Long id) {
        Alquiler alquiler = alquilerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alquiler", id));
        if (alquiler.getEstado() == EstadoAlquiler.CANCELADA) {
            throw new BusinessException("No se pueden registrar pagos sobre un alquiler CANCELADO",
                    HttpStatus.BAD_REQUEST);
        }
        return alquiler;
    }

    /** REGLA: no se paga un catering CANCELADO. */
    private CateringServicio validarCateringPagable(Long id) {
        CateringServicio catering = cateringRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio de catering", id));
        if (catering.getEstado() == EstadoCatering.CANCELADO) {
            throw new BusinessException("No se pueden registrar pagos sobre un catering CANCELADO",
                    HttpStatus.BAD_REQUEST);
        }
        return catering;
    }

    private int contarReferencias(PagoCreateDTO dto) {
        int n = 0;
        if (dto.inscripcionId() != null) n++;
        if (dto.cotizacionId() != null) n++;
        if (dto.alquilerId() != null) n++;
        if (dto.cateringId() != null) n++;
        return n;
    }

    // ─────────────── helpers ───────────────

    private Specification<Pago> conFiltros(EstadoPago estado, MetodoPago metodoPago, Long inscripcionId) {
        return (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (estado != null) {
                condiciones.add(cb.equal(root.get("estado"), estado));
            }
            if (metodoPago != null) {
                condiciones.add(cb.equal(root.get("metodoPago"), metodoPago));
            }
            if (inscripcionId != null) {
                condiciones.add(cb.equal(root.get("inscripcion").get("id"), inscripcionId));
            }
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
    }

    /** TODO(producción): secuencia de BD para evitar colisiones concurrentes. */
    private String generarNumeroRecibo() {
        return String.format("REC-%d-%06d", LocalDate.now().getYear(), pagoRepository.count() + 1);
    }

    private String normalizarOpcional(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    private Pago buscarEntidad(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago", id));
    }
}