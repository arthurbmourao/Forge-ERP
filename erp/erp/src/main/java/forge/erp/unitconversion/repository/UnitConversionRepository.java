package forge.erp.unitconversion.repository;

import forge.erp.unitconversion.model.UnitConversion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UnitConversionRepository extends JpaRepository<UnitConversion, UUID> {

    Optional<UnitConversion> findByFromUnitIdAndToUnitId(UUID fromUnitId, UUID toUnitId);
}
