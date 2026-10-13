package cl.respirarysoltar.pagos.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Evento que envía la pasarela de pago al webhook público. En producción lleva una
 * firma que se valida contra el secreto de la pasarela antes de procesar (zona PCI).
 */
public record WebhookRequest(
        @NotNull(message = "El id del pago es obligatorio")
        UUID pagoId,

        @NotNull(message = "El evento es obligatorio")
        EventoPasarela evento
) {
    /** Eventos que la pasarela puede notificar. */
    public enum EventoPasarela {
        PAGO_CONFIRMADO,
        PAGO_RECHAZADO
    }
}
