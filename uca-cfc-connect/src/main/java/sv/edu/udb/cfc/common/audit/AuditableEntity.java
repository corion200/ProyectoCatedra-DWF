package sv.edu.udb.cfc.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Superclase mapeada (no es una entidad propia) que centraliza los
 * campos de auditoría estándar para todas las entidades del sistema.
 * <p>
 * Requiere que {@code @EnableJpaAuditing} esté activo en la clase principal
 * (ver {@code UcaCfcConnectApplication}).
 * <p>
 * Uso: {@code public class Curso extends AuditableEntity { ... }}
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

}
