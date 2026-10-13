package cl.respirarysoltar.pagos.api.dto;

import cl.respirarysoltar.pagos.domain.TipoReferencia;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/** Datos de entrada para iniciar el cobro de una cita o taller (RF-08). */
public record PagoRequest(
        @NotNull(message = "La referencia (cita o taller) es obligatoria")
        UUID referenciaId,

        @NotNull(message = "El tipo de referencia es obligatorio (CITA o TALLER)")
        TipoReferencia tipo,

        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor que cero")
        BigDecimal monto
) {
}
