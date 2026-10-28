package forge.erp.productunit.repository;

import forge.erp.productunit.model.ProductUnit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductUnitRepository extends JpaRepository<ProductUnit, UUID> {

    boolean existsByProductId(UUID productId);

    // "unit" e carregada junto porque o mapper le code/symbol fora do contexto de persistencia
    @EntityGraph(attributePaths = "unit")
    List<ProductUnit> findAllByProductId(UUID productId);

    @EntityGraph(attributePaths = "unit")
    Optional<ProductUnit> findByIdAndProductId(UUID id, UUID productId);

    @EntityGraph(attributePaths = "unit")
    Optional<ProductUnit> findByProductIdAndUnitId(UUID productId, UUID unitId);
}
