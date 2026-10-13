package cl.respirarysoltar.pagos.service;

import cl.respirarysoltar.pagos.api.dto.PagoRequest;
import cl.respirarysoltar.pagos.api.dto.ReembolsoRequest;
import cl.respirarysoltar.pagos.api.dto.WebhookRequest;
import cl.respirarysoltar.pagos.domain.EstadoPago;
import cl.respirarysoltar.pagos.domain.Pago;
import cl.respirarysoltar.pagos.domain.Reembolso;
import cl.respirarysoltar.pagos.domain.TipoReferencia;
import cl.respirarysoltar.pagos.exception.ReglaNegocioException;
import cl.respirarysoltar.pagos.repository.PagoRepository;
import cl.respirarysoltar.pagos.repository.ReembolsoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private ReembolsoRepository reembolsoRepository;

    @InjectMocks
    private PagoService service;

    private Pago pagoPendiente() {
        return new Pago(UUID.randomUUID(), TipoReferencia.CITA, new BigDecimal("35000"));
    }

    @Test
    void crearDejaElPagoPendienteConTokenDePasarela() {
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        Pago creado = service.crear(new PagoRequest(UUID.randomUUID(), TipoReferencia.CITA, new BigDecimal("35000")));

        assertEquals(EstadoPago.PENDIENTE, creado.getEstado());
        assertNotNull(creado.getTokenPasarela());
    }

    @Test
    void webhookConfirmaUnPagoPendiente() {
        Pago pago = pagoPendiente();
        when(pagoRepository.findById(any())).thenReturn(Optional.of(pago));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        Pago confirmado = service.procesarWebhook(
                new WebhookRequest(UUID.randomUUID(), WebhookRequest.EventoPasarela.PAGO_CONFIRMADO));

        assertEquals(EstadoPago.CONFIRMADO, confirmado.getEstado());
    }

    @Test
    void webhookSobrePagoYaProcesadoLanzaConflicto() {
        Pago pago = pagoPendiente();
        pago.setEstado(EstadoPago.CONFIRMADO);
        when(pagoRepository.findById(any())).thenReturn(Optional.of(pago));

        WebhookRequest request = new WebhookRequest(UUID.randomUUID(), WebhookRequest.EventoPasarela.PAGO_CONFIRMADO);
        assertThrows(ReglaNegocioException.class, () -> service.procesarWebhook(request));
    }

    @Test
    void reembolsoTotalMarcaElPagoComoReembolsado() {
        Pago pago = pagoPendiente();
        pago.setEstado(EstadoPago.CONFIRMADO);
        when(pagoRepository.findById(any())).thenReturn(Optional.of(pago));
        lenient().when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reembolsoRepository.save(any(Reembolso.class))).thenAnswer(inv -> inv.getArgument(0));

        Reembolso reembolso = service.reembolsar(UUID.randomUUID(), new ReembolsoRequest(new BigDecimal("35000"), "Cancelación de la cita"));

        assertNotNull(reembolso);
        assertEquals(EstadoPago.REEMBOLSADO, pago.getEstado());
    }

    @Test
    void reembolsarUnPagoPendienteLanzaConflicto() {
        Pago pago = pagoPendiente();
        when(pagoRepository.findById(any())).thenReturn(Optional.of(pago));

        ReembolsoRequest request = new ReembolsoRequest(new BigDecimal("1000"), "motivo");
        assertThrows(ReglaNegocioException.class, () -> service.reembolsar(UUID.randomUUID(), request));
    }
}
