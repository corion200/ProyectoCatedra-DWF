package sv.edu.udb.cfc.venue.service;

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
import sv.edu.udb.cfc.quotation.entity.Cotizacion;
import sv.edu.udb.cfc.quotation.enums.EstadoCotizacion;
import sv.edu.udb.cfc.quotation.enums.TipoCotizacion;
import sv.edu.udb.cfc.quotation.repository.CotizacionRepository;
import sv.edu.udb.cfc.shared.exception.BusinessException;
import sv.edu.udb.cfc.shared.exception.ResourceNotFoundException;
import sv.edu.udb.cfc.venue.dto.AlquilerCreateDTO;
import sv.edu.udb.cfc.venue.dto.AlquilerResponseDTO;
import sv.edu.udb.cfc.venue.dto.AlquilerUpdateDTO;
import sv.edu.udb.cfc.venue.entity.Alquiler;
import sv.edu.udb.cfc.venue.entity.Espacio;
import sv.edu.udb.cfc.venue.enums.EstadoAlquiler;
import sv.edu.udb.cfc.venue.repository.AlquilerRepository;
import sv.edu.udb.cfc.venue.repository.EspacioRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlquilerServiceImpl implements AlquilerService {

    private final AlquilerRepository alquilerRepository;
    private final EspacioRepository espacioRepository;
    private final ClienteRepository clienteRepository;
    private final CotizacionRepository cotizacionRepository;

    /** REGLA: solo estos estados bloquean el horario de un espacio. */
    private static final Set<EstadoAlquiler> ESTADOS_QUE_BLOQUEAN =
            EnumSet.of(EstadoAlquiler.PENDIENTE, EstadoAlquiler.CONFIRMADA);

    @Override
    @Transactional
    public AlquilerResponseDTO crear(AlquilerCreateDTO dto) {
        Espacio espacio = validarEspacioReservable(dto.espacioId());
        Cliente cliente = validarClienteActivo(dto.clienteId());
        Cotizacion cotizacion = validarCotizacionOrigen(dto.cotizacionId());
        validarRangoHoras(dto.horaInicio(), dto.horaFin());
        // REGLA CENTRAL: el mismo espacio no puede tener dos reservas que se crucen
        validarSolapamiento(espacio.getId(), dto.fechaEvento(), dto.horaInicio(), dto.horaFin(), null);

        Alquiler alquiler = Alquiler.builder()
                .codigo(generarCodigo())
                .espacio(espacio)
                .cliente(cliente)
                .cotizacion(cotizacion)
                .fechaEvento(dto.fechaEvento())
                .horaInicio(dto.horaInicio())
                .horaFin(dto.horaFin())
                .costoTotal(calcularCosto(espacio.getPrecioHora(), dto.horaInicio(), dto.horaFin()))
                .observaciones(dto.observaciones())
                .build();   // estado nace en PENDIENTE
        return AlquilerResponseDTO.from(alquilerRepository.save(alquiler));
    }

    @Override
    @Transactional
    public AlquilerResponseDTO actualizar(Long id, AlquilerUpdateDTO dto) {
        Alquiler alquiler = buscarEntidad(id);
        if (alquiler.getEstado() == EstadoAlquiler.CANCELADA
                || alquiler.getEstado() == EstadoAlquiler.FINALIZADA) {
            throw new BusinessException("No se puede editar un alquiler en estado " + alquiler.getEstado(),
                    HttpStatus.BAD_REQUEST);
        }
        validarRangoHoras(dto.horaInicio(), dto.horaFin());
        // Re-valida solapamiento EXCLUYÉNDOSE a sí mismo de la comparación
        validarSolapamiento(alquiler.getEspacio().getId(), dto.fechaEvento(),
                dto.horaInicio(), dto.horaFin(), id);

        alquiler.setFechaEvento(dto.fechaEvento());
        alquiler.setHoraInicio(dto.horaInicio());
        alquiler.setHoraFin(dto.horaFin());
        alquiler.setCostoTotal(calcularCosto(alquiler.getEspacio().getPrecioHora(),
                dto.horaInicio(), dto.horaFin()));   // el costo se recalcula si cambian las horas
        alquiler.setObservaciones(dto.observaciones());
        return AlquilerResponseDTO.from(alquiler);
    }

    @Override
    public AlquilerResponseDTO obtenerPorId(Long id) {
        return AlquilerResponseDTO.from(buscarEntidad(id));
    }

    @Override
    public Page<AlquilerResponseDTO> listar(Long clienteId, Long espacioId,
                                            EstadoAlquiler estado, LocalDate fecha, Pageable pageable) {
        return alquilerRepository.findAll(conFiltros(clienteId, espacioId, estado, fecha), pageable)
                .map(AlquilerResponseDTO::from);
    }

    @Override
    public List<AlquilerResponseDTO> ocupacionDeEspacio(Long espacioId, LocalDate fecha) {
        if (!espacioRepository.existsById(espacioId)) {
            throw new ResourceNotFoundException("Espacio", espacioId);
        }
        return alquilerRepository
                .findByEspacioIdAndFechaEventoAndEstadoIn(espacioId, fecha, ESTADOS_QUE_BLOQUEAN)
                .stream().map(AlquilerResponseDTO::from).toList();
    }

    @Override
    @Transactional
    public AlquilerResponseDTO confirmar(Long id) {
        Alquiler alquiler = buscarEntidad(id);
        if (alquiler.getEstado() != EstadoAlquiler.PENDIENTE) {
            throw new BusinessException("Solo se confirman alquileres en estado PENDIENTE (actual: "
                    + alquiler.getEstado() + ")", HttpStatus.BAD_REQUEST);
        }
        alquiler.setEstado(EstadoAlquiler.CONFIRMADA);
        return AlquilerResponseDTO.from(alquiler);
    }

    @Override
    @Transactional
    public AlquilerResponseDTO finalizar(Long id) {
        Alquiler alquiler = buscarEntidad(id);
        if (alquiler.getEstado() != EstadoAlquiler.CONFIRMADA) {
            throw new BusinessException("Solo se finalizan alquileres en estado CONFIRMADA (actual: "
                    + alquiler.getEstado() + ")", HttpStatus.BAD_REQUEST);
        }
        alquiler.setEstado(EstadoAlquiler.FINALIZADA);
        return AlquilerResponseDTO.from(alquiler);
    }

    @Override
    @Transactional
    public AlquilerResponseDTO cancelar(Long id, String motivo) {
        Alquiler alquiler = buscarEntidad(id);
        if (alquiler.getEstado() == EstadoAlquiler.CANCELADA
                || alquiler.getEstado() == EstadoAlquiler.FINALIZADA) {
            throw new BusinessException("No se puede cancelar un alquiler en estado " + alquiler.getEstado(),
                    HttpStatus.BAD_REQUEST);
        }
        alquiler.setEstado(EstadoAlquiler.CANCELADA);   // libera el horario (deja de bloquear)
        if (motivo != null && !motivo.isBlank()) {
            alquiler.setObservaciones(motivo);
        }
        return AlquilerResponseDTO.from(alquiler);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Alquiler alquiler = buscarEntidad(id);
        if (alquiler.getEstado() != EstadoAlquiler.PENDIENTE) {
            throw new BusinessException("Solo se pueden eliminar alquileres PENDIENTES. "
                    + "Los demás: cancélelos para conservar el historial.", HttpStatus.BAD_REQUEST);
        }
        alquilerRepository.delete(alquiler);
    }

    // ─────────────── reglas de negocio ───────────────

    private Espacio validarEspacioReservable(Long espacioId) {
        Espacio espacio = espacioRepository.findById(espacioId)
                .orElseThrow(() -> new ResourceNotFoundException("Espacio", espacioId));
        if (!Boolean.TRUE.equals(espacio.getActivo()) || !Boolean.TRUE.equals(espacio.getDisponible())) {
            throw new BusinessException("El espacio no está disponible para reservas",
                    HttpStatus.BAD_REQUEST);
        }
        return espacio;
    }

    private Cliente validarClienteActivo(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteId));
        if (!Boolean.TRUE.equals(cliente.getActivo())) {
            throw new BusinessException("El cliente se encuentra inactivo", HttpStatus.BAD_REQUEST);
        }
        return cliente;
    }

    /** REGLA: si el alquiler proviene de una cotización, debe estar APROBADA y ser de tipo ALQUILER_ESPACIO. */
    private Cotizacion validarCotizacionOrigen(Long cotizacionId) {
        if (cotizacionId == null) {
            return null;
        }
        Cotizacion cotizacion = cotizacionRepository.findById(cotizacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización", cotizacionId));
        if (cotizacion.getEstado() != EstadoCotizacion.APROBADA) {
            throw new BusinessException("La cotización debe estar APROBADA para generar un alquiler (actual: "
                    + cotizacion.getEstado() + ")", HttpStatus.BAD_REQUEST);
        }
        if (cotizacion.getTipo() != TipoCotizacion.ALQUILER_ESPACIO) {
            throw new BusinessException("La cotización es de tipo " + cotizacion.getTipo()
                    + " y no corresponde a ALQUILER_ESPACIO", HttpStatus.BAD_REQUEST);
        }
        return cotizacion;
    }

    /** REGLA: dos reservas se solapan si inicio < finExistente Y fin > inicioExistente (intervalos semiabiertos). */
    private void validarSolapamiento(Long espacioId, LocalDate fecha,
                                     java.time.LocalTime inicio, java.time.LocalTime fin, Long idExcluido) {
        List<Alquiler> reservasDelDia = alquilerRepository
                .findByEspacioIdAndFechaEventoAndEstadoIn(espacioId, fecha, ESTADOS_QUE_BLOQUEAN);
        for (Alquiler reserva : reservasDelDia) {
            if (idExcluido != null && reserva.getId().equals(idExcluido)) {
                continue;   // al editar, la propia reserva no cuenta como conflicto
            }
            boolean seSolapa = inicio.isBefore(reserva.getHoraFin())
                    && fin.isAfter(reserva.getHoraInicio());
            if (seSolapa) {
                throw new BusinessException("El espacio ya está reservado ese día de "
                        + reserva.getHoraInicio() + " a " + reserva.getHoraFin()
                        + " (reserva " + reserva.getCodigo() + ")");
            }
        }
    }

    private void validarRangoHoras(java.time.LocalTime inicio, java.time.LocalTime fin) {
        if (!fin.isAfter(inicio)) {
            throw new BusinessException("La hora de fin debe ser posterior a la hora de inicio",
                    HttpStatus.BAD_REQUEST);
        }
    }

    /** costoTotal = precioHora × (duración en minutos / 60), redondeado a 2 decimales. */
    private BigDecimal calcularCosto(BigDecimal precioHora, java.time.LocalTime inicio,
                                     java.time.LocalTime fin) {
        long minutos = Duration.between(inicio, fin).toMinutes();
        BigDecimal horas = BigDecimal.valueOf(minutos)
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        return precioHora.multiply(horas).setScale(2, RoundingMode.HALF_UP);
    }

    // ─────────────── helpers ───────────────

    private Specification<Alquiler> conFiltros(Long clienteId, Long espacioId,
                                               EstadoAlquiler estado, LocalDate fecha) {
        return (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (clienteId != null) {
                condiciones.add(cb.equal(root.get("cliente").get("id"), clienteId));
            }
            if (espacioId != null) {
                condiciones.add(cb.equal(root.get("espacio").get("id"), espacioId));
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
        return String.format("ALQ-%d-%05d", LocalDate.now().getYear(),
                alquilerRepository.count() + 1);
    }

    private Alquiler buscarEntidad(Long id) {
        return alquilerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alquiler", id));
    }
}