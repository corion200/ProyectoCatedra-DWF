package sv.edu.udb.cfc.catering.dto;

import sv.edu.udb.cfc.catering.entity.CateringServicio;
import sv.edu.udb.cfc.catering.enums.EstadoCatering;
import sv.edu.udb.cfc.catering.enums.TipoServicioCatering;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record CateringResponseDTO(
        Long id, String codigo, Long clienteId, String clienteNombre, Long cotizacionId,
        TipoServicioCatering tipoServicio, Integer numeroAsistentes, String menu,
        LocalDate fechaEvento, LocalTime horaEntrega, String lugar,
        BigDecimal precioPorPersona, BigDecimal costoTotal, EstadoCatering estado,
        String observaciones) {

    public static CateringResponseDTO from(CateringServicio c) {
        return new CateringResponseDTO(c.getId(), c.getCodigo(),
                c.getCliente().getId(), c.getCliente().getNombre(),
                c.getCotizacion() != null ? c.getCotizacion().getId() : null,
                c.getTipoServicio(), c.getNumeroAsistentes(), c.getMenu(),
                c.getFechaEvento(), c.getHoraEntrega(), c.getLugar(),
                c.getPrecioPorPersona(), c.getCostoTotal(), c.getEstado(), c.getObservaciones());
    }
}