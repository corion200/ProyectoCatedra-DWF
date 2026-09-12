package sv.edu.udb.cfc.venue.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sv.edu.udb.cfc.venue.enums.TipoEspacio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "espacios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Espacio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoEspacio tipo;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(name = "precio_hora", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioHora;

    /** Proyector, A/C, PCs, etc. */
    @Column(length = 1000)
    private String equipamiento;

    @Builder.Default
    @Column(nullable = false)
    private Boolean disponible = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}