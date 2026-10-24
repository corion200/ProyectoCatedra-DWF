package sv.edu.udb.cfc.catering.dto;

import sv.edu.udb.cfc.catering.entity.CateringServicio;
import sv.edu.udb.cfc.catering.enums.EstadoCatering;
import sv.edu.udb.cfc.catering.enums.TipoServicioCatering;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record CateringResponseDTO( // Datos del catering que se devuelven
                                   Long id, String codigo, Long clienteId, String clienteNombre, Long cotizacionId, // Identificadores y cliente
                                   TipoServicioCatering tipoServicio, Integer numeroAsistentes, String menu, // Tipo, personas y menú
                                   LocalDate fechaEvento, LocalTime horaEntrega, String lugar, // Cuándo y dónde
                                   BigDecimal precioPorPersona, BigDecimal costoTotal, EstadoCatering estado, // Precios y estado
                                   String observaciones) { // Notas extra

    public static CateringResponseDTO from(CateringServicio c) { // Convierte la entidad en DTO
        return new CateringResponseDTO(c.getId(), c.getCodigo(),
                c.getCliente().getId(), c.getCliente().getNombre(), // Datos del cliente
                c.getCotizacion() != null ? c.getCotizacion().getId() : null, // Null si no hay cotización
                c.getTipoServicio(), c.getNumeroAsistentes(), c.getMenu(),
                c.getFechaEvento(), c.getHoraEntrega(), c.getLugar(),
                c.getPrecioPorPersona(), c.getCostoTotal(), c.getEstado(), c.getObservaciones());
    }
}