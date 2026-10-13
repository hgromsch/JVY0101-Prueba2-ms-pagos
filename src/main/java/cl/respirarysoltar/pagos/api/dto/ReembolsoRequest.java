package cl.respirarysoltar.pagos.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Datos de entrada para reembolsar (total o parcialmente) un pago confirmado. */
public record ReembolsoRequest(
        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor que cero")
        BigDecimal monto,

        @NotBlank(message = "El motivo del reembolso es obligatorio")
        @Size(max = 250)
        String motivo
) {
}
