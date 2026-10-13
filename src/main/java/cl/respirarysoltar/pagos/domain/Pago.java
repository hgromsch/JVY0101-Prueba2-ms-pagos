package cl.respirarysoltar.pagos.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Pago de una cita o inscripción a taller (RF-08). Zona PCI: nunca se persisten
 * datos de tarjeta; solo el token opaco que entrega la pasarela de pago.
 */
@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue
    private UUID id;

    /** Id de la cita o inscripción que origina el cobro. */
    @Column(nullable = false)
    private UUID referenciaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoReferencia tipo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoPago estado;

    /** Token opaco de la transacción en la pasarela; jamás datos de tarjeta. */
    @Column(length = 80)
    private String tokenPasarela;

    @Column(nullable = false)
    private LocalDateTime creadoEn;

    /** Relación OneToMany: reembolsos (totales o parciales) aplicados al pago. */
    @OneToMany(mappedBy = "pago", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reembolso> reembolsos = new ArrayList<>();

    protected Pago() {
        // Constructor requerido por JPA
    }

    public Pago(UUID referenciaId, TipoReferencia tipo, BigDecimal monto) {
        this.referenciaId = referenciaId;
        this.tipo = tipo;
        this.monto = monto;
        this.estado = EstadoPago.PENDIENTE;
    }

    @PrePersist
    void alCrear() {
        if (creadoEn == null) {
            creadoEn = LocalDateTime.now();
        }
    }

    public void agregarReembolso(Reembolso reembolso) {
        reembolso.setPago(this);
        reembolsos.add(reembolso);
    }

    public BigDecimal totalReembolsado() {
        return reembolsos.stream().map(Reembolso::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public UUID getId() {
        return id;
    }

    public UUID getReferenciaId() {
        return referenciaId;
    }

    public TipoReferencia getTipo() {
        return tipo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }

    public String getTokenPasarela() {
        return tokenPasarela;
    }

    public void setTokenPasarela(String tokenPasarela) {
        this.tokenPasarela = tokenPasarela;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public List<Reembolso> getReembolsos() {
        return reembolsos;
    }
}
