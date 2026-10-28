package forge.erp.product.service;

import forge.erp.category.model.Category;
import forge.erp.category.repository.CategoryRepository;
import forge.erp.common.code.CodeGenerator;
import forge.erp.common.exception.BusinessRuleException;
import forge.erp.common.exception.ResourceNotFoundException;
import forge.erp.product.dto.ProductRequest;
import forge.erp.product.dto.ProductResponse;
import forge.erp.product.mapper.ProductMapper;
import forge.erp.product.model.Product;
import forge.erp.product.repository.ProductRepository;
import forge.erp.productunit.repository.ProductUnitRepository;
import forge.erp.unit.model.Unit;
import forge.erp.unit.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final String CODE_PREFIX = "PRD";

    private final ProductMapper mapper;
    private final ProductRepository repository;
    private final CategoryRepository categoryRepository;
    private final UnitRepository unitRepository;
    private final ProductUnitRepository productUnitRepository;
    private final CodeGenerator codeGenerator;

    @Transactional
    public ProductResponse saveProduct(ProductRequest request) {
        validateStockRange(request);

        Category category = resolveCategory(request.categoryId(), null);
        Unit stockUnit = resolveStockUnit(request.stockUnitId(), null);

        Product product = mapper.toEntity(request, category, stockUnit);
        product.setCode(codeGenerator.generate(CODE_PREFIX, repository::existsByCode));

        product = repository.save(product);

        return mapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id) {
        return mapper.toResponse(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = getOrThrow(id);

        validateStockRange(request);

        Category category = resolveCategory(request.categoryId(), product.getCategory());
        Unit stockUnit = resolveStockUnit(request.stockUnitId(), product.getStockUnit());

        // as conversoes dos ProductUnit sao relativas a unidade de estoque:
        // trocar a unidade de estoque as tornaria invalidas
        boolean stockUnitChanged = !stockUnit.getId().equals(product.getStockUnit().getId());
        if (stockUnitChanged && productUnitRepository.existsByProductId(id)) {
            throw new BusinessRuleException(
                    "Não é possível alterar a unidade de estoque de um produto que possui unidades vinculadas.");
        }

        // code e gerado no servidor e imutavel: nao e alterado no update
        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(category);
        product.setStockUnit(stockUnit);
        product.setWeight(request.weight());
        product.setMinimumStock(request.minimumStock());
        product.setMaximumStock(request.maximumStock());
        product.setActive(request.active());
        product.setUpdatedAt(LocalDateTime.now());

        product = repository.save(product);

        return mapper.toResponse(product);
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException.of("Produto", id);
        }
        repository.deleteById(id);
    }

    @Transactional
    public void turnOn(UUID id) {
        Product product = getOrThrow(id);
        product.setActive(true);
        product.setUpdatedAt(LocalDateTime.now());
    }

    @Transactional
    public void turnOff(UUID id) {
        Product product = getOrThrow(id);
        product.setActive(false);
        product.setUpdatedAt(LocalDateTime.now());
    }

    private Product getOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Produto", id));
    }

    private void validateStockRange(ProductRequest request) {
        if (request.minimumStock() != null
                && request.maximumStock() != null
                && request.maximumStock().compareTo(request.minimumStock()) < 0) {
            throw new BusinessRuleException("O estoque máximo não pode ser menor que o estoque mínimo.");
        }
    }

    /**
     * Busca a categoria. Exige que esteja ativa apenas quando for uma categoria nova
     * para o produto (para nao impedir edicoes de produtos cuja categoria foi inativada depois).
     */
    private Category resolveCategory(UUID categoryId, Category current) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> ResourceNotFoundException.of("Categoria", categoryId));

        boolean changed = current == null || !category.getId().equals(current.getId());
        if (changed && !Boolean.TRUE.equals(category.getActive())) {
            throw new BusinessRuleException("A categoria informada está inativa.");
        }
        return category;
    }

    private Unit resolveStockUnit(UUID unitId, Unit current) {
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> ResourceNotFoundException.of("Unidade", unitId));

        boolean changed = current == null || !unit.getId().equals(current.getId());
        if (changed && !unit.isActive()) {
            throw new BusinessRuleException("A unidade de estoque informada está inativa.");
        }
        return unit;
    }
}
