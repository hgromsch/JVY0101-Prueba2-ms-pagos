package cl.respirarysoltar.pagos.repository;

import cl.respirarysoltar.pagos.domain.Reembolso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReembolsoRepository extends JpaRepository<Reembolso, UUID> {

    List<Reembolso> findByPagoId(UUID pagoId);
}
