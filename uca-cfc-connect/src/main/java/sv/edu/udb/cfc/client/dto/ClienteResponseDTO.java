package sv.edu.udb.cfc.client.dto;

import sv.edu.udb.cfc.client.entity.Cliente;
import sv.edu.udb.cfc.client.enums.TipoCliente;

import java.time.LocalDateTime;

public record ClienteResponseDTO(
        Long id, TipoCliente tipoCliente, String nombre, String dui, String nit,
        String correo, String telefono, String direccion, String contactoNombre,
        Boolean activo, LocalDateTime fechaCreacion) {

    public static ClienteResponseDTO from(Cliente c) {
        return new ClienteResponseDTO(c.getId(), c.getTipoCliente(), c.getNombre(), c.getDui(), c.getNit(),
                c.getCorreo(), c.getTelefono(), c.getDireccion(), c.getContactoNombre(),
                c.getActivo(), c.getFechaCreacion());
    }
}