package sv.edu.udb.cfc.client.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import sv.edu.udb.cfc.client.enums.TipoCliente;

public record ClienteUpdateDTO(
        @NotNull(message = "El tipo de cliente es obligatorio") TipoCliente tipoCliente,
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 200) String nombre,
        @Pattern(regexp = "^\\d{8}-\\d$", message = "Formato de DUI inválido (########-#)") String dui,
        @NotBlank(message = "El NIT es obligatorio") @Size(max = 17) String nit,
        @NotBlank @Email(message = "Correo electrónico inválido") @Size(max = 150) String correo,
        @NotBlank(message = "El teléfono es obligatorio") @Size(max = 15) String telefono,
        @Size(max = 300) String direccion,
        @Size(max = 150) String contactoNombre,
        Boolean activo) {
}