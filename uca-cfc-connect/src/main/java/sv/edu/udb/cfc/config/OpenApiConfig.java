package sv.edu.udb.cfc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cfcOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("UCA-CFC Connect API")
                .description("API Fase 2 - Centro de Formación Continua, Universidad Don Bosco")
                .version("2.0.0")
                .contact(new Contact().name("Equipo de Desarrollo UDB")));
    }
}