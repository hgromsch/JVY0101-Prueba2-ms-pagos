package cl.respirarysoltar.pagos.repository;

import cl.respirarysoltar.pagos.domain.EstadoPago;
import cl.respirarysoltar.pagos.domain.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PagoRepository extends JpaRepository<Pago, UUID> {

    List<Pago> findByReferenciaId(UUID referenciaId);

    List<Pago> findByEstado(EstadoPago estado);
}
