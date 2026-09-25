package sv.edu.udb.cfc.venue.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.udb.cfc.client.entity.Cliente;
import sv.edu.udb.cfc.quotation.repository.CotizacionRepository;
import sv.edu.udb.cfc.shared.exception.BusinessException;
import sv.edu.udb.cfc.venue.dto.AlquilerCreateDTO;
import sv.edu.udb.cfc.venue.dto.AlquilerResponseDTO;
import sv.edu.udb.cfc.venue.entity.Alquiler;
import sv.edu.udb.cfc.venue.entity.Espacio;
import sv.edu.udb.cfc.venue.repository.AlquilerRepository;
import sv.edu.udb.cfc.venue.repository.EspacioRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/** Casos del enunciado (Fase 3): éxito, fallo por solapamiento y fallo por negocio. */
@ExtendWith(MockitoExtension.class)
class AlquilerServiceImplTest {

    @Mock private AlquilerRepository alquilerRepository;
    @Mock private EspacioRepository espacioRepository;
    @Mock private sv.edu.udb.cfc.client.repository.ClienteRepository clienteRepository;
    @Mock private CotizacionRepository cotizacionRepository;
    @InjectMocks private AlquilerServiceImpl alquilerService;

    private static final LocalDate FECHA = LocalDate.of(2026, 10, 20);

    private Espacio espacioDisponible() {
        return Espacio.builder().id(1L).nombre("Auditorio Principal")
                .activo(true).disponible(true)
                .precioHora(new BigDecimal("150.00")).build();
    }

    private AlquilerCreateDTO peticion(LocalTime inicio, LocalTime fin) {
        return new AlquilerCreateDTO(1L, 1L, null, FECHA, inicio, fin, null);
    }

    @Test
    @DisplayName("Caso de éxito: registra la reserva cuando el espacio y horario están libres")
    void registrarReserva_exitoso_cuandoHorarioLibre() {
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacioDisponible()));
        when(clienteRepository.findById(1L))
                .thenReturn(Optional.of(Cliente.builder().id(1L).activo(true).nombre("María").build()));
        when(alquilerRepository.findByEspacioIdAndFechaEventoAndEstadoIn(
                eq(1L), eq(FECHA), anyCollection())).thenReturn(List.of());
        when(alquilerRepository.save(any(Alquiler.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AlquilerResponseDTO resultado =
                alquilerService.crear(peticion(LocalTime.of(9, 0), LocalTime.of(12, 0)));

        assertEquals(0, resultado.costoTotal().compareTo(new BigDecimal("450.00")), // 3h × 150
                "El costo debe ser precioHora × horas");
        assertEquals(sv.edu.udb.cfc.venue.enums.EstadoAlquiler.PENDIENTE, resultado.estado());
    }

    @Test
    @DisplayName("Caso de fallo por solapamiento: rechaza si ya existe una reserva en ese horario")
    void registrarReserva_falla_cuandoHaySolapamiento() {
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacioDisponible()));
        when(clienteRepository.findById(1L))
                .thenReturn(Optional.of(Cliente.builder().id(1L).activo(true).build()));
        Alquiler existente = Alquiler.builder().id(99L).codigo("ALQ-X")
                .horaInicio(LocalTime.of(10, 0)).horaFin(LocalTime.of(11, 0)).build();
        when(alquilerRepository.findByEspacioIdAndFechaEventoAndEstadoIn(
                eq(1L), eq(FECHA), anyCollection())).thenReturn(List.of(existente));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> alquilerService.crear(peticion(LocalTime.of(9, 0), LocalTime.of(12, 0))));
        assertTrue(ex.getMessage().contains("ya está reservado"),
                "El mensaje debe indicar que el espacio ya está reservado");
    }

    @Test
    @DisplayName("Caso de fallo por negocio: rechaza si el espacio no está disponible")
    void registrarReserva_falla_cuandoEspacioNoDisponible() {
        Espacio espacio = espacioDisponible();
        espacio.setDisponible(false);
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacio));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> alquilerService.crear(peticion(LocalTime.of(9, 0), LocalTime.of(12, 0))));
        assertTrue(ex.getMessage().contains("no está disponible"));
    }
}