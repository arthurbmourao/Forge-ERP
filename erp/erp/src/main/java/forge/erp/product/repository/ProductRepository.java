package forge.erp.product.repository;

import forge.erp.product.model.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsByCode(String code);

    // carrega category e stockUnit junto, evitando N+1 e LazyInitializationException no mapper
    @Override
    @EntityGraph(attributePaths = {"category", "stockUnit"})
    List<Product> findAll();

    @Override
    @EntityGraph(attributePaths = {"category", "stockUnit"})
    Optional<Product> findById(UUID id);
}
