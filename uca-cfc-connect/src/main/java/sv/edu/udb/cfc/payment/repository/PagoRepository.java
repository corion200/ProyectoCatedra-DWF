package sv.edu.udb.cfc.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.edu.udb.cfc.payment.entity.Pago;
import sv.edu.udb.cfc.payment.enums.EstadoPago;

import java.math.BigDecimal;
import java.util.Collection;

public interface PagoRepository extends JpaRepository<Pago, Long>, JpaSpecificationExecutor<Pago> {

    /**
     * Suma los montos aplicables de una inscripción (PARCIAL + PAGADO)
     * para calcular el saldo pendiente contra el precio del curso.
     * COALESCE devuelve 0 si no hay pagos aún.
     */
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p " +
            "WHERE p.inscripcion.id = :inscripcionId AND p.estado IN :estados")
    BigDecimal sumarMontosPorInscripcion(@Param("inscripcionId") Long inscripcionId,
                                         @Param("estados") Collection<EstadoPago> estados);
}