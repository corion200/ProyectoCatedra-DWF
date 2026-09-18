package sv.edu.udb.cfc.venue.dto;

import sv.edu.udb.cfc.venue.entity.Espacio;
import sv.edu.udb.cfc.venue.enums.TipoEspacio;

import java.math.BigDecimal;

public record EspacioResponseDTO(
        Long id, String codigo, String nombre, TipoEspacio tipo, Integer capacidad,
        BigDecimal precioHora, String equipamiento, Boolean disponible, Boolean activo) {

    public static EspacioResponseDTO from(Espacio e) {
        return new EspacioResponseDTO(e.getId(), e.getCodigo(), e.getNombre(), e.getTipo(),
                e.getCapacidad(), e.getPrecioHora(), e.getEquipamiento(),
                e.getDisponible(), e.getActivo());
    }
}