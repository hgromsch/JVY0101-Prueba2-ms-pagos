package cl.respirarysoltar.pagos.api;

import cl.respirarysoltar.pagos.api.dto.PagoRequest;
import cl.respirarysoltar.pagos.api.dto.ReembolsoRequest;
import cl.respirarysoltar.pagos.api.dto.WebhookRequest;
import cl.respirarysoltar.pagos.domain.EstadoPago;
import cl.respirarysoltar.pagos.domain.Pago;
import cl.respirarysoltar.pagos.domain.Reembolso;
import cl.respirarysoltar.pagos.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * API REST de cobros de sesiones y talleres (RF-08). El endpoint /webhook es público
 * y lo invoca la pasarela de pago; en producción se valida la firma del remitente.
 */
@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService service;

    public PagoController(PagoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Pago> crear(@Valid @RequestBody PagoRequest request) {
        Pago creado = service.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @GetMapping
    public List<Pago> listar(@RequestParam(required = false) EstadoPago estado) {
        return service.listar(estado);
    }

    @GetMapping("/{id}")
    public Pago obtener(@PathVariable UUID id) {
        return service.obtener(id);
    }

    /** Webhook que invoca la pasarela para confirmar o rechazar un pago. */
    @PostMapping("/webhook")
    public Pago webhook(@Valid @RequestBody WebhookRequest request) {
        return service.procesarWebhook(request);
    }

    @PostMapping("/{id}/reembolsos")
    public ResponseEntity<Reembolso> reembolsar(@PathVariable UUID id,
                                                @Valid @RequestBody ReembolsoRequest request) {
        Reembolso reembolso = service.reembolsar(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reembolso);
    }

    @GetMapping("/{id}/reembolsos")
    public List<Reembolso> listarReembolsos(@PathVariable UUID id) {
        return service.listarReembolsos(id);
    }
}
