package sv.edu.udb.cfc.catering.entity;

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
import sv.edu.udb.cfc.catering.enums.EstadoCatering;
import sv.edu.udb.cfc.catering.enums.TipoServicioCatering;
import sv.edu.udb.cfc.client.entity.Cliente;
import sv.edu.udb.cfc.quotation.entity.Cotizacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

// Servicio de catering que contrata un cliente (tabla catering_servicios)
@Entity
@Table(name = "catering_servicios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CateringServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // código de negocio, no se puede repetir
    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    // cliente que contrata, obligatorio
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // puede venir de una cotización, pero no es obligatorio
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cotizacion_id")
    private Cotizacion cotizacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_servicio", nullable = false, length = 20)
    private TipoServicioCatering tipoServicio;

    @Column(name = "numero_asistentes", nullable = false)
    private Integer numeroAsistentes;

    @Column(nullable = false, length = 1000)
    private String menu;

    @Column(name = "fecha_evento", nullable = false)
    private LocalDate fechaEvento;

    @Column(name = "hora_entrega", nullable = false)
    private LocalTime horaEntrega;

    @Column(nullable = false, length = 255)
    private String lugar;

    @Column(name = "precio_por_persona", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioPorPersona;

    // se calcula en el servicio: precioPorPersona × numeroAsistentes
    @Column(name = "costo_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoTotal;

    @Column(length = 500)
    private String observaciones;

    // arranca en PENDIENTE (el Builder.Default evita que el builder lo deje null)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoCatering estado = EstadoCatering.PENDIENTE;

    // las fechas las maneja Hibernate solo
    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}