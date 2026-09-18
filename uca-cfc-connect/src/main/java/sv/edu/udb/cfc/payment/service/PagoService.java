package sv.edu.udb.cfc.payment.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sv.edu.udb.cfc.payment.dto.PagoCreateDTO;
import sv.edu.udb.cfc.payment.dto.PagoResponseDTO;
import sv.edu.udb.cfc.payment.dto.ResumenPagoDTO;
import sv.edu.udb.cfc.payment.enums.EstadoPago;
import sv.edu.udb.cfc.payment.enums.MetodoPago;

public interface PagoService {

    PagoResponseDTO registrar(PagoCreateDTO dto);

    PagoResponseDTO obtenerPorId(Long id);

    Page<PagoResponseDTO> listar(EstadoPago estado, MetodoPago metodoPago,
                                 Long inscripcionId, Pageable pageable);

    PagoResponseDTO cambiarEstado(Long id, EstadoPago nuevoEstado);

    void eliminar(Long id);

    /** Estado de cuenta de una inscripción: precio del curso, pagado y saldo. */
    ResumenPagoDTO resumenInscripcion(Long inscripcionId);
}