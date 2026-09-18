package sv.edu.udb.cfc.payment.dto;

import sv.edu.udb.cfc.payment.entity.Pago;
import sv.edu.udb.cfc.payment.enums.EstadoPago;
import sv.edu.udb.cfc.payment.enums.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoResponseDTO(
        Long id, String codigo, Long inscripcionId, Long cotizacionId,
        Long alquilerId, Long cateringId, BigDecimal monto, MetodoPago metodoPago,
        EstadoPago estado, LocalDateTime fechaPago, String numeroComprobante,
        String observaciones) {

    public static PagoResponseDTO from(Pago p) {
        return new PagoResponseDTO(p.getId(), p.getCodigo(),
                p.getInscripcion() != null ? p.getInscripcion().getId() : null,
                p.getCotizacion() != null ? p.getCotizacion().getId() : null,
                p.getAlquiler() != null ? p.getAlquiler().getId() : null,
                p.getCatering() != null ? p.getCatering().getId() : null,
                p.getMonto(), p.getMetodoPago(), p.getEstado(), p.getFechaPago(),
                p.getNumeroComprobante(), p.getObservaciones());
    }
}