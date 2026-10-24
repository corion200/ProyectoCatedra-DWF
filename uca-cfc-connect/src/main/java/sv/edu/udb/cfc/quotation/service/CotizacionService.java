package sv.edu.udb.cfc.quotation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sv.edu.udb.cfc.quotation.dto.CotizacionCreateDTO;
import sv.edu.udb.cfc.quotation.dto.CotizacionResponseDTO;
import sv.edu.udb.cfc.quotation.dto.CotizacionUpdateDTO;
import sv.edu.udb.cfc.quotation.enums.EstadoCotizacion;
import sv.edu.udb.cfc.quotation.enums.TipoCotizacion;
/**
 * Contrato del módulo de Cotizaciones: define las operaciones disponibles
 * (crear, actualizar, consultar, listar con filtros, cambiar estado y eliminar).
 * La implementación está en {@code CotizacionServiceImpl}.
 */
public interface CotizacionService {

    CotizacionResponseDTO crear(CotizacionCreateDTO dto);

    CotizacionResponseDTO actualizar(Long id, CotizacionUpdateDTO dto);

    CotizacionResponseDTO obtenerPorId(Long id);

    Page<CotizacionResponseDTO> listar(Long clienteId, TipoCotizacion tipo,
                                       EstadoCotizacion estado, Pageable pageable);

    /** Máquina de estados: PENDIENTE → EN_PROCESO → APROBADA/RECHAZADA. */
    CotizacionResponseDTO cambiarEstado(Long id, EstadoCotizacion nuevoEstado);

    void eliminar(Long id);
}