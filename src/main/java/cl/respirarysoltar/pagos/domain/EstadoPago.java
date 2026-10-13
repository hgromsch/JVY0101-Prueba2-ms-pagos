package cl.respirarysoltar.pagos.domain;

/** Ciclo de vida de un pago (RF-08). */
public enum EstadoPago {
    PENDIENTE,
    CONFIRMADO,
    RECHAZADO,
    REEMBOLSADO
}
