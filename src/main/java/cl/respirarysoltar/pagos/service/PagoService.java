package cl.respirarysoltar.pagos.service;

import cl.respirarysoltar.pagos.api.dto.PagoRequest;
import cl.respirarysoltar.pagos.api.dto.ReembolsoRequest;
import cl.respirarysoltar.pagos.api.dto.WebhookRequest;
import cl.respirarysoltar.pagos.domain.EstadoPago;
import cl.respirarysoltar.pagos.domain.Pago;
import cl.respirarysoltar.pagos.domain.Reembolso;
import cl.respirarysoltar.pagos.exception.RecursoNoEncontradoException;
import cl.respirarysoltar.pagos.exception.ReglaNegocioException;
import cl.respirarysoltar.pagos.repository.PagoRepository;
import cl.respirarysoltar.pagos.repository.ReembolsoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Lógica de negocio de pagos (RF-08). Zona PCI: no se manejan ni persisten datos
 * de tarjeta; la confirmación llega vía webhook firmado de la pasarela y se
 * publica "pago_confirmado" a EventBridge (TODO AWS) para liberar la agenda.
 */
@Service
@Transactional
public class PagoService {

    private final PagoRepository pagoRepository;
    private final ReembolsoRepository reembolsoRepository;

    public PagoService(PagoRepository pagoRepository, ReembolsoRepository reembolsoRepository) {
        this.pagoRepository = pagoRepository;
        this.reembolsoRepository = reembolsoRepository;
    }

    /** Inicia el cobro; queda PENDIENTE hasta que la pasarela confirme por webhook. */
    public Pago crear(PagoRequest request) {
        Pago pago = new Pago(request.referenciaId(), request.tipo(), request.monto());
        // En producción el token lo entrega la pasarela al crear la intención de pago.
        pago.setTokenPasarela("tok_simulado_" + UUID.randomUUID());
        return pagoRepository.save(pago);
    }

    @Transactional(readOnly = true)
    public List<Pago> listar(EstadoPago estado) {
        return estado == null ? pagoRepository.findAll() : pagoRepository.findByEstado(estado);
    }

    @Transactional(readOnly = true)
    public Pago obtener(UUID id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago no encontrado: " + id));
    }

    /** Confirma o rechaza el pago según el evento notificado por la pasarela. */
    public Pago procesarWebhook(WebhookRequest request) {
        // TODO (AWS): validar la firma del webhook contra el secreto de la pasarela
        // antes de procesar. Nunca loguear datos de tarjeta.
        Pago pago = obtener(request.pagoId());
        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            throw new ReglaNegocioException("El pago ya fue procesado (estado actual: " + pago.getEstado() + ")");
        }

        switch (request.evento()) {
            case PAGO_CONFIRMADO -> {
                pago.setEstado(EstadoPago.CONFIRMADO);
                // TODO (AWS): publicar evento "pago_confirmado" a EventBridge.
            }
            case PAGO_RECHAZADO -> pago.setEstado(EstadoPago.RECHAZADO);
        }
        return pagoRepository.save(pago);
    }

    /** Reembolsa total o parcialmente un pago confirmado (p.ej. por cancelación de cita). */
    public Reembolso reembolsar(UUID pagoId, ReembolsoRequest request) {
        Pago pago = obtener(pagoId);
        if (pago.getEstado() != EstadoPago.CONFIRMADO) {
            throw new ReglaNegocioException("Solo se pueden reembolsar pagos CONFIRMADOS (estado actual: " + pago.getEstado() + ")");
        }
        if (pago.totalReembolsado().add(request.monto()).compareTo(pago.getMonto()) > 0) {
            throw new ReglaNegocioException("El monto acumulado de reembolsos superaría el monto del pago");
        }

        Reembolso reembolso = new Reembolso(request.monto(), request.motivo());
        pago.agregarReembolso(reembolso);
        if (pago.totalReembolsado().compareTo(pago.getMonto()) == 0) {
            pago.setEstado(EstadoPago.REEMBOLSADO);
        }
        pagoRepository.save(pago);
        // TODO (AWS): ejecutar el reembolso real en la pasarela y publicar evento.
        return reembolsoRepository.save(reembolso);
    }

    @Transactional(readOnly = true)
    public List<Reembolso> listarReembolsos(UUID pagoId) {
        obtener(pagoId);
        return reembolsoRepository.findByPagoId(pagoId);
    }
}
