package sv.edu.udb.cfc.security.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    /** Hash BCrypt — NUNCA la contraseña plana. */
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    /** true = la contraseña fue generada por el sistema y DEBE cambiarse en el próximo login. */
    @Builder.Default
    @Column(name = "password_temporal", nullable = false)
    private Boolean passwordTemporal = false;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)   // EAGER: el filtro lo necesita en cada request
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
}