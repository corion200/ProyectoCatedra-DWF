package sv.edu.udb.cfc.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sv.edu.udb.cfc.catering.entity.CateringServicio;
import sv.edu.udb.cfc.enrollment.entity.Inscripcion;
import sv.edu.udb.cfc.payment.enums.EstadoPago;
import sv.edu.udb.cfc.payment.enums.MetodoPago;
import sv.edu.udb.cfc.quotation.entity.Cotizacion;
import sv.edu.udb.cfc.venue.entity.Alquiler;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Pago asociado a EXACTAMENTE UN documento (inscripción, cotización,
 * alquiler o catering). La regla la fuerza el CHECK ck_pago_documento
 * en la BD y se valida además en la capa de servicio.
 */
@Entity
@Table(name = "pagos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Número de recibo. */
    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inscripcion_id")
    private Inscripcion inscripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cotizacion_id")
    private Cotizacion cotizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alquiler_id")
    private Alquiler alquiler;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catering_id")
    private CateringServicio catering;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 20)
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoPago estado = EstadoPago.PENDIENTE;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Column(name = "numero_comprobante", length = 50)
    private String numeroComprobante;

    @Column(length = 500)
    private String observaciones;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}