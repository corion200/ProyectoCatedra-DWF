package sv.edu.udb.cfc.venue.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)  // desactiva JWT solo para los tests
class AlquilerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /api/v1/alquileres: sin fechaEvento retorna 400 (@NotNull)")
    void crearAlquiler_sinFecha_retorna400() throws Exception {
        String body = """
                {
                  "espacioId": 1,
                  "clienteId": 1,
                  "horaInicio": "08:00:00",
                  "horaFin": "12:00:00"
                }
                """;

        mockMvc.perform(post("/api/v1/alquileres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/alquileres: con fecha en el pasado retorna 400 (@FutureOrPresent)")
    void crearAlquiler_conFechaPasada_retorna400() throws Exception {
        String body = """
                {
                  "espacioId": 1,
                  "clienteId": 1,
                  "fechaEvento": "2020-01-15",
                  "horaInicio": "08:00:00",
                  "horaFin": "12:00:00"
                }
                """;

        mockMvc.perform(post("/api/v1/alquileres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/alquileres: sin espacioId retorna 400")
    void crearAlquiler_sinEspacio_retorna400() throws Exception {
        String body = """
                {
                  "clienteId": 1,
                  "fechaEvento": "2026-12-01",
                  "horaInicio": "08:00:00",
                  "horaFin": "12:00:00"
                }
                """;

        mockMvc.perform(post("/api/v1/alquileres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/alquileres: con horaFin anterior a horaInicio retorna 400")
    void crearAlquiler_conHorarioInvalido_retorna400() throws Exception {
        String body = """
                {
                  "espacioId": 1,
                  "clienteId": 1,
                  "fechaEvento": "2026-12-01",
                  "horaInicio": "14:00:00",
                  "horaFin": "10:00:00"
                }
                """;

        mockMvc.perform(post("/api/v1/alquileres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}