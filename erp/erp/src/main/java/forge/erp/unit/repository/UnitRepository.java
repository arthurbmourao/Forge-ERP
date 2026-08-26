package forge.erp.unit.repository;

import forge.erp.unit.model.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.rmi.server.UID;
import java.util.UUID;

public interface UnitRepository extends JpaRepository<Unit, UUID> {
}
