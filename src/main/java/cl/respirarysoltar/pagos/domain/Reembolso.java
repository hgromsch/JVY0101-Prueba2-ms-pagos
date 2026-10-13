package cl.respirarysoltar.pagos.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Reembolso (total o parcial) asociado a un pago confirmado: se genera cuando un
 * paciente cancela una cita o taller dentro del plazo permitido (RF-05, RF-08).
 * Lado "muchos" de la relación ManyToOne con Pago.
 */
@Entity
@Table(name = "reembolsos")
public class Reembolso {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 250)
    private String motivo;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pago_id", nullable = false)
    private Pago pago;

    protected Reembolso() {
        // Constructor requerido por JPA
    }

    public Reembolso(BigDecimal monto, String motivo) {
        this.monto = monto;
        this.motivo = motivo;
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Pago getPago() {
        return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }
}
