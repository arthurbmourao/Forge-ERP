package forge.erp.productunit.service;

import forge.erp.common.exception.BusinessRuleException;
import forge.erp.common.exception.ConflictException;
import forge.erp.common.exception.ResourceNotFoundException;
import forge.erp.product.model.Product;
import forge.erp.product.repository.ProductRepository;
import forge.erp.productunit.dto.ProductUnitRequest;
import forge.erp.productunit.dto.ProductUnitResponse;
import forge.erp.productunit.mapper.ProductUnitMapper;
import forge.erp.productunit.model.ProductUnit;
import forge.erp.productunit.repository.ProductUnitRepository;
import forge.erp.unit.model.Unit;
import forge.erp.unit.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductUnitService {

    private final ProductUnitMapper mapper;
    private final ProductUnitRepository repository;
    private final ProductRepository productRepository;
    private final UnitRepository unitRepository;

    @Transactional
    public ProductUnitResponse save(UUID productId, ProductUnitRequest request) {
        Product product = getProductOrThrow(productId);

        if (!product.isActive()) {
            throw new BusinessRuleException("O produto está inativo.");
        }

        Unit unit = resolveUnit(product, request.unitId(), null);

        ProductUnit productUnit = repository.save(mapper.toEntity(request, product, unit));

        return mapper.toResponse(productUnit);
    }

    @Transactional(readOnly = true)
    public List<ProductUnitResponse> findAll(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw ResourceNotFoundException.of("Produto", productId);
        }
        return repository.findAllByProductId(productId).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductUnitResponse findById(UUID productId, UUID id) {
        return mapper.toResponse(getOrThrow(productId, id));
    }

    @Transactional
    public ProductUnitResponse update(UUID productId, UUID id, ProductUnitRequest request) {
        ProductUnit productUnit = getOrThrow(productId, id);
        Product product = getProductOrThrow(productId);

        Unit unit = resolveUnit(product, request.unitId(), productUnit);

        productUnit.setUnit(unit);
        productUnit.setConversionFactor(request.conversionFactor());
        productUnit.setPurchaseUnit(request.purchaseUnit());
        productUnit.setIssueUnit(request.issueUnit());
        productUnit.setActive(request.active());

        return mapper.toResponse(repository.save(productUnit));
    }

    @Transactional
    public void deleteById(UUID productId, UUID id) {
        repository.delete(getOrThrow(productId, id));
    }

    @Transactional
    public void turnOn(UUID productId, UUID id) {
        getOrThrow(productId, id).setActive(true);
    }

    @Transactional
    public void turnOff(UUID productId, UUID id) {
        getOrThrow(productId, id).setActive(false);
    }

    private Product getProductOrThrow(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> ResourceNotFoundException.of("Produto", productId));
    }

    private ProductUnit getOrThrow(UUID productId, UUID id) {
        return repository.findByIdAndProductId(id, productId)
                .orElseThrow(() -> ResourceNotFoundException.of("Unidade do produto", id));
    }

    /**
     * Busca a unidade e valida as regras do ProductUnit:
     * existe, esta ativa (quando for nova para o vinculo), nao e a unidade de estoque do produto,
     * tem o mesmo UnitType da unidade de estoque e nao esta duplicada para o produto.
     *
     * @param current vinculo sendo editado (null na criacao)
     */
    private Unit resolveUnit(Product product, UUID unitId, ProductUnit current) {
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> ResourceNotFoundException.of("Unidade", unitId));

        boolean changed = current == null || !unit.getId().equals(current.getUnit().getId());
        if (changed && !unit.isActive()) {
            throw new BusinessRuleException("A unidade informada está inativa.");
        }

        Unit stockUnit = product.getStockUnit();

        if (unit.getId().equals(stockUnit.getId())) {
            throw new BusinessRuleException(
                    "A unidade informada é a própria unidade de estoque do produto.");
        }

        if (unit.getType() != stockUnit.getType()) {
            throw new BusinessRuleException(
                    "A unidade informada deve ser do mesmo tipo da unidade de estoque do produto (%s)."
                            .formatted(stockUnit.getType()));
        }

        UUID currentId = current == null ? null : current.getId();
        repository.findByProductIdAndUnitId(product.getId(), unitId)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ConflictException("Esta unidade já está vinculada ao produto.");
                });

        return unit;
    }
}
