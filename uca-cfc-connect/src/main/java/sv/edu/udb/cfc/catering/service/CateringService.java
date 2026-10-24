package sv.edu.udb.cfc.catering.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sv.edu.udb.cfc.catering.dto.CateringCreateDTO;
import sv.edu.udb.cfc.catering.dto.CateringResponseDTO;
import sv.edu.udb.cfc.catering.dto.CateringUpdateDTO;
import sv.edu.udb.cfc.catering.enums.EstadoCatering;

import java.time.LocalDate;

// Lógica de negocio de los servicios de catering
public interface CateringService {

    // registra un servicio nuevo (el costo total se calcula aquí)
    CateringResponseDTO crear(CateringCreateDTO dto);

    // modifica los datos de un servicio existente
    CateringResponseDTO actualizar(Long id, CateringUpdateDTO dto);

    CateringResponseDTO obtenerPorId(Long id);

    // listado paginado, los filtros (cliente, estado, fecha) son opcionales
    Page<CateringResponseDTO> listar(Long clienteId, EstadoCatering estado, LocalDate fecha, Pageable pageable);

    // cambios de estado
    CateringResponseDTO confirmar(Long id);

    CateringResponseDTO finalizar(Long id);

    // guarda el motivo de la cancelación
    CateringResponseDTO cancelar(Long id, String motivo);

    void eliminar(Long id);
}