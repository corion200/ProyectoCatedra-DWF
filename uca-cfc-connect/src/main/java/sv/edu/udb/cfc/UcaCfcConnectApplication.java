package sv.edu.udb.cfc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Punto de entrada principal del sistema UCA-CFC-Connect.
 * <p>
 * Sistema web empresarial para la gestión integral del Centro de Formación
 * Continua (CFC) de la universidad: gestión académica, clientes, inscripciones,
 * cotizaciones, alquiler de espacios, catering, agenda institucional y pagos.
 * </p>
 *
 * @author Arquitectura de Software - UCA CFC
 */
@SpringBootApplication
@EnableJpaAuditing
public class UcaCfcConnectApplication {

    public static void main(String[] args) {
        SpringApplication.run(UcaCfcConnectApplication.class, args);
    }

}
