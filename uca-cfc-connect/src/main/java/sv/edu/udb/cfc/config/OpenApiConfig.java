package sv.edu.udb.cfc.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SCHEME = "bearerAuth";

    @Bean
    public OpenAPI cfcOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("UCA-CFC Connect API")
                        .description("Centro de Formación Continua UDB — API con seguridad JWT y roles. "
                                + "Haga login en /api/v1/auth/login y pegue el token en el botón Authorize.")
                        .version("3.0.0")
                        .contact(new Contact().name("Equipo de Desarrollo UDB")))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME))
                .components(new Components().addSecuritySchemes(SCHEME,
                        new SecurityScheme().name(SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}

