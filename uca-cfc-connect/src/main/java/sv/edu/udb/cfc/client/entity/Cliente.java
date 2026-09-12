package sv.edu.udb.cfc.client.entity;

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
import sv.edu.udb.cfc.client.enums.TipoCliente;

import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cliente", nullable = false, length = 10)
    private TipoCliente tipoCliente;

    /** Nombre completo (PERSONA) o razón social (EMPRESA). */
    @Column(nullable = false, length = 200)
    private String nombre;

    /** Obligatorio para PERSONA (validado en servicio); la BD lo deja nullable. */
    @Column(unique = true, length = 10)
    private String dui;

    @Column(nullable = false, unique = true, length = 17)
    private String nit;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(nullable = false, length = 15)
    private String telefono;

    @Column(length = 300)
    private String direccion;

    /** Persona de contacto (aplica a EMPRESA). */
    @Column(name = "contacto_nombre", length = 150)
    private String contactoNombre;

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