package sv.edu.udb.cfc.venue.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.udb.cfc.venue.dto.EspacioCreateDTO;
import sv.edu.udb.cfc.venue.dto.EspacioResponseDTO;
import sv.edu.udb.cfc.venue.entity.Espacio;
import sv.edu.udb.cfc.venue.enums.TipoEspacio;
import sv.edu.udb.cfc.venue.repository.EspacioRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EspacioServiceTest {

    @Mock
    private EspacioRepository espacioRepository;

    /** Si tu implementación se llama distinto, cambiá esta clase (ej. EspacioServiceImpl). */
    @InjectMocks
    private EspacioServiceImpl espacioService;

    private EspacioCreateDTO dtoValido;

    @BeforeEach
    void setUp() {
        // Record: constructor posicional con los 7 campos del DTO
        dtoValido = new EspacioCreateDTO(
                "SAL-001",
                "Sala de capacitación A",
                TipoEspacio.SALA,
                30,
                new BigDecimal("150.00"),
                "Proyector, A/C",
                true);
    }

    @Test
    @DisplayName("crear: con datos válidos guarda el espacio y retorna la respuesta")
    void crear_conDatosValidos_guardaYRetornaRespuesta() {
        when(espacioRepository.save(any(Espacio.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        EspacioResponseDTO resultado = espacioService.crear(dtoValido);

        assertThat(resultado).isNotNull();
        verify(espacioRepository, times(1)).save(any(Espacio.class));
    }

    @Test
    @DisplayName("crear: con código duplicado lanza excepción y no guarda")
    void crear_conCodigoDuplicado_lanzaExcepcion() {
        Espacio existente = Espacio.builder()
                .id(1L).codigo("SAL-001").nombre("Sala A")
                .tipo(TipoEspacio.SALA).capacidad(30)
                .precioHora(new BigDecimal("150.00"))
                .build();

        lenient().when(espacioRepository.findByCodigo("SAL-001"))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> espacioService.crear(dtoValido))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("obtenerPorId: con id existente retorna la respuesta del espacio")
    void obtenerPorId_existente_retornaRespuesta() {
        Espacio entidad = Espacio.builder()
                .id(5L).codigo("SAL-001").nombre("Sala de capacitación A")
                .tipo(TipoEspacio.SALA).capacidad(30)
                .precioHora(new BigDecimal("150.00"))
                .disponible(true).activo(true)
                .build();

        when(espacioRepository.findById(5L)).thenReturn(Optional.of(entidad));

        EspacioResponseDTO resultado = espacioService.obtenerPorId(5L);

        assertThat(resultado).isNotNull();
        verify(espacioRepository, times(1)).findById(5L);
    }

    @Test
    @DisplayName("obtenerPorId: con id inexistente lanza excepción")
    void obtenerPorId_inexistente_lanzaExcepcion() {
        when(espacioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> espacioService.obtenerPorId(99L))
                .isInstanceOf(RuntimeException.class);
    }
}