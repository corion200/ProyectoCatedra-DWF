package sv.edu.udb.cfc.client.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sv.edu.udb.cfc.client.dto.ClienteCreateDTO;
import sv.edu.udb.cfc.client.dto.ClienteResponseDTO;
import sv.edu.udb.cfc.client.dto.ClienteUpdateDTO;
import sv.edu.udb.cfc.client.enums.TipoCliente;

public interface ClienteService {

    ClienteResponseDTO crear(ClienteCreateDTO dto);

    ClienteResponseDTO actualizar(Long id, ClienteUpdateDTO dto);

    ClienteResponseDTO obtenerPorId(Long id);

    /** Filtros combinables: nombre (contiene), tipo y estado activo. */
    Page<ClienteResponseDTO> listar(String nombre, TipoCliente tipo, Boolean activo, Pageable pageable);

    void eliminar(Long id);
}