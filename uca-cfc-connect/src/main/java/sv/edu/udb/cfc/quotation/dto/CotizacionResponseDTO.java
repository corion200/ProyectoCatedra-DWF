package sv.edu.udb.cfc.quotation.dto;

import sv.edu.udb.cfc.quotation.entity.Cotizacion;
import sv.edu.udb.cfc.quotation.enums.EstadoCotizacion;
import sv.edu.udb.cfc.quotation.enums.TipoCotizacion;

import java.math.BigDecimal;
import java.time.LocalDate;
/**
 * Respuesta que se devuelve al cliente de la API. Incluye el id y código generado,
 * el id y nombre del cliente, y el estado actual. El método {@code from} convierte la
 * entidad {@code Cotizacion} a este DTO para no exponer la entidad directamente.
 */
public record CotizacionResponseDTO(
        Long id, String codigo, Long clienteId, String clienteNombre,
        TipoCotizacion tipo, String descripcion, EstadoCotizacion estado,
        LocalDate fechaEvento, BigDecimal montoEstimado, String observaciones) {

    public static CotizacionResponseDTO from(Cotizacion c) {
        return new CotizacionResponseDTO(c.getId(), c.getCodigo(),
                c.getCliente().getId(), c.getCliente().getNombre(),
                c.getTipo(), c.getDescripcion(), c.getEstado(),
                c.getFechaEvento(), c.getMontoEstimado(), c.getObservaciones());
    }
}