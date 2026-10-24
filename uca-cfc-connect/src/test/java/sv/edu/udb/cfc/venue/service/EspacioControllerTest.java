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
class EspacioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /api/v1/espacios: con datos válidos crea el espacio")
    void crearEspacio_conDatosValidos_crea() throws Exception {
        String body = """
                {
                  "codigo": "TST-01",
                  "nombre": "Sala de prueba",
                  "tipo": "SALA",
                  "capacidad": 20,
                  "precioHora": 50.00,
                  "equipamiento": "Proyector",
                  "disponible": true
                }
                """;

        mockMvc.perform(post("/api/v1/espacios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.codigo").value("TST-01"));
    }

    @Test
    @DisplayName("POST /api/v1/espacios: sin tipo retorna 400 (validación @NotNull)")
    void crearEspacio_sinTipo_retorna400() throws Exception {
        String body = """
                {
                  "codigo": "TST-02",
                  "nombre": "Sala sin tipo",
                  "capacidad": 10,
                  "precioHora": 50.00
                }
                """;

        mockMvc.perform(post("/api/v1/espacios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/espacios: con tipo SALON invalido retorna 400")
    void crearEspacio_conEnumInvalido_retorna400() throws Exception {
        String body = """
                {
                  "codigo": "TST-03",
                  "nombre": "Sala SALON",
                  "tipo": "SALON",
                  "capacidad": 10,
                  "precioHora": 50.00
                }
                """;

        mockMvc.perform(post("/api/v1/espacios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/espacios: con capacidad 0 retorna 400 (validación @Positive)")
    void crearEspacio_conCapacidadInvalida_retorna400() throws Exception {
        String body = """
                {
                  "codigo": "TST-04",
                  "nombre": "Sala capacidad cero",
                  "tipo": "SALA",
                  "capacidad": 0,
                  "precioHora": 50.00
                }
                """;

        mockMvc.perform(post("/api/v1/espacios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}