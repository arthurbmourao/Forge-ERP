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
import forge.erp.unit.enums.UnitType;
import forge.erp.unit.model.Unit;
import forge.erp.unit.repository.UnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductMapper mapper;

    @Mock
    private ProductRepository repository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private ProductUnitRepository productUnitRepository;

    @Mock
    private CodeGenerator codeGenerator;

    @InjectMocks
    private ProductService service;

    private Category category;
    private Unit stockUnit;

    @BeforeEach
    void setUp() {
        category = new Category("CAT-0001", "Fixadores", "Parafusos");
        category.setId(UUID.randomUUID());
        category.setActive(true);

        stockUnit = new Unit("UNI-0001", "Unidade", "UN", UnitType.QUANTITY, 0, true);
        stockUnit.setId(UUID.randomUUID());
    }

    private Product sampleProduct() {
        Product product = new Product("PRD-0001", "Parafuso M8", "desc", category, stockUnit,
                null, new BigDecimal("10"), new BigDecimal("100"), true);
        product.setId(UUID.randomUUID());
        return product;
    }

    private ProductRequest requestFor(UUID categoryId, UUID unitId) {
        return new ProductRequest("Parafuso Sextavado M8", "nova desc", categoryId, unitId,
                null, new BigDecimal("10"), new BigDecimal("100"), true);
    }

    private ProductResponse responseOf(Product p) {
        return new ProductResponse(p.getId(), p.getCode(), p.getName(), p.getDescription(),
                p.getCategory().getId(), p.getCategory().getName(),
                p.getStockUnit().getId(), p.getStockUnit().getSymbol(),
                p.getWeight(), p.getMinimumStock(), p.getMaximumStock(), p.isActive(),
                p.getCreatedAt(), p.getUpdatedAt());
    }

    // ---------- saveProduct ----------

    @Test
    @DisplayName("saveProduct deve gerar code com prefixo PRD, persistir e retornar a resposta")
    void saveProductShouldGenerateCodePersistAndReturnResponse() {
        ProductRequest request = requestFor(category.getId(), stockUnit.getId());
        Product entity = new Product(null, "Parafuso Sextavado M8", "nova desc", category, stockUnit,
                null, new BigDecimal("10"), new BigDecimal("100"), true);
        ProductResponse expected = responseOf(entity);

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(unitRepository.findById(stockUnit.getId())).thenReturn(Optional.of(stockUnit));
        when(mapper.toEntity(request, category, stockUnit)).thenReturn(entity);
        when(codeGenerator.generate(eq("PRD"), any())).thenReturn("PRD-0149");
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(expected);

        ProductResponse result = service.saveProduct(request);

        assertThat(result).isEqualTo(expected);
        assertThat(entity.getCode()).isEqualTo("PRD-0149");
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("saveProduct deve lancar 404 quando a categoria nao existe")
    void saveProductShouldFailWhenCategoryMissing() {
        ProductRequest request = requestFor(category.getId(), stockUnit.getId());
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.saveProduct(request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("saveProduct deve rejeitar categoria inativa")
    void saveProductShouldRejectInactiveCategory() {
        category.setActive(false);
        ProductRequest request = requestFor(category.getId(), stockUnit.getId());
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> service.saveProduct(request))
                .isInstanceOf(BusinessRuleException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("saveProduct deve lancar 404 quando a unidade de estoque nao existe")
    void saveProductShouldFailWhenUnitMissing() {
        ProductRequest request = requestFor(category.getId(), stockUnit.getId());
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(unitRepository.findById(stockUnit.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.saveProduct(request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("saveProduct deve rejeitar unidade de estoque inativa")
    void saveProductShouldRejectInactiveUnit() {
        stockUnit.setActive(false);
        ProductRequest request = requestFor(category.getId(), stockUnit.getId());
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(unitRepository.findById(stockUnit.getId())).thenReturn(Optional.of(stockUnit));

        assertThatThrownBy(() -> service.saveProduct(request))
                .isInstanceOf(BusinessRuleException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("saveProduct deve rejeitar estoque maximo menor que o minimo")
    void saveProductShouldRejectMaximumBelowMinimum() {
        ProductRequest request = new ProductRequest("Parafuso", null, category.getId(), stockUnit.getId(),
                null, new BigDecimal("100"), new BigDecimal("10"), true);

        assertThatThrownBy(() -> service.saveProduct(request))
                .isInstanceOf(BusinessRuleException.class);
        verifyNoInteractions(categoryRepository, unitRepository, repository);
    }

    @Test
    @DisplayName("saveProduct deve aceitar estoque minimo e maximo nulos")
    void saveProductShouldAcceptNullStockLimits() {
        ProductRequest request = new ProductRequest("Parafuso", null, category.getId(), stockUnit.getId(),
                null, null, null, true);
        Product entity = new Product(null, "Parafuso", null, category, stockUnit, null, null, null, true);
        ProductResponse expected = responseOf(entity);

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(unitRepository.findById(stockUnit.getId())).thenReturn(Optional.of(stockUnit));
        when(mapper.toEntity(request, category, stockUnit)).thenReturn(entity);
        when(codeGenerator.generate(eq("PRD"), any())).thenReturn("PRD-0002");
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(expected);

        assertThat(service.saveProduct(request)).isEqualTo(expected);
    }

    // ---------- findById / findAll ----------

    @Test
    @DisplayName("findById deve retornar o produto existente")
    void findByIdShouldReturnExisting() {
        Product product = sampleProduct();
        ProductResponse expected = responseOf(product);
        when(repository.findById(product.getId())).thenReturn(Optional.of(product));
        when(mapper.toResponse(product)).thenReturn(expected);

        assertThat(service.findById(product.getId())).isEqualTo(expected);
    }

    @Test
    @DisplayName("findById deve lancar 404 quando nao encontrado")
    void findByIdShouldThrowWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("findAll deve mapear todos os produtos")
    void findAllShouldMapAll() {
        Product product = sampleProduct();
        ProductResponse expected = responseOf(product);
        when(repository.findAll()).thenReturn(List.of(product));
        when(mapper.toResponse(product)).thenReturn(expected);

        assertThat(service.findAll()).containsExactly(expected);
    }

    // ---------- update ----------

    @Test
    @DisplayName("update deve aplicar os valores do request e preservar o code")
    void updateShouldApplyRequestValuesAndKeepCode() {
        Product product = sampleProduct();
        ProductRequest request = new ProductRequest("Parafuso Novo", "outra desc", category.getId(),
                stockUnit.getId(), new BigDecimal("0.5"), new BigDecimal("5"), new BigDecimal("50"), false);

        when(repository.findById(product.getId())).thenReturn(Optional.of(product));
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(unitRepository.findById(stockUnit.getId())).thenReturn(Optional.of(stockUnit));
        when(repository.save(product)).thenReturn(product);
        when(mapper.toResponse(product)).thenReturn(responseOf(product));

        service.update(product.getId(), request);

        assertThat(product.getCode()).isEqualTo("PRD-0001");
        assertThat(product.getName()).isEqualTo("Parafuso Novo");
        assertThat(product.getDescription()).isEqualTo("outra desc");
        assertThat(product.getWeight()).isEqualByComparingTo("0.5");
        assertThat(product.getMinimumStock()).isEqualByComparingTo("5");
        assertThat(product.getMaximumStock()).isEqualByComparingTo("50");
        assertThat(product.isActive()).isFalse();
        verify(repository).save(product);
    }

    @Test
    @DisplayName("update nao deve exigir categoria ativa quando a categoria nao mudou")
    void updateShouldAllowUnchangedInactiveCategory() {
        Product product = sampleProduct();
        category.setActive(false);
        ProductRequest request = requestFor(category.getId(), stockUnit.getId());

        when(repository.findById(product.getId())).thenReturn(Optional.of(product));
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(unitRepository.findById(stockUnit.getId())).thenReturn(Optional.of(stockUnit));
        when(repository.save(product)).thenReturn(product);
        when(mapper.toResponse(product)).thenReturn(responseOf(product));

        service.update(product.getId(), request);

        verify(repository).save(product);
    }

    @Test
    @DisplayName("update deve rejeitar troca para categoria inativa")
    void updateShouldRejectChangeToInactiveCategory() {
        Product product = sampleProduct();
        Category other = new Category("CAT-0002", "Outra", "x");
        other.setId(UUID.randomUUID());
        other.setActive(false);
        ProductRequest request = requestFor(other.getId(), stockUnit.getId());

        when(repository.findById(product.getId())).thenReturn(Optional.of(product));
        when(categoryRepository.findById(other.getId())).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.update(product.getId(), request))
                .isInstanceOf(BusinessRuleException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update deve bloquear troca da unidade de estoque quando existem ProductUnit")
    void updateShouldBlockStockUnitChangeWhenProductUnitsExist() {
        Product product = sampleProduct();
        Unit newUnit = new Unit("UNI-0009", "Quilo", "kg", UnitType.MASS, 3, true);
        newUnit.setId(UUID.randomUUID());
        ProductRequest request = requestFor(category.getId(), newUnit.getId());

        when(repository.findById(product.getId())).thenReturn(Optional.of(product));
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(unitRepository.findById(newUnit.getId())).thenReturn(Optional.of(newUnit));
        when(productUnitRepository.existsByProductId(product.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.update(product.getId(), request))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(product.getStockUnit()).isSameAs(stockUnit);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update deve permitir troca da unidade de estoque quando nao existem ProductUnit")
    void updateShouldAllowStockUnitChangeWithoutProductUnits() {
        Product product = sampleProduct();
        Unit newUnit = new Unit("UNI-0009", "Quilo", "kg", UnitType.MASS, 3, true);
        newUnit.setId(UUID.randomUUID());
        ProductRequest request = requestFor(category.getId(), newUnit.getId());

        when(repository.findById(product.getId())).thenReturn(Optional.of(product));
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(unitRepository.findById(newUnit.getId())).thenReturn(Optional.of(newUnit));
        when(productUnitRepository.existsByProductId(product.getId())).thenReturn(false);
        when(repository.save(product)).thenReturn(product);
        when(mapper.toResponse(product)).thenReturn(responseOf(product));

        service.update(product.getId(), request);

        assertThat(product.getStockUnit()).isSameAs(newUnit);
    }

    @Test
    @DisplayName("update deve lancar 404 quando o produto nao existe")
    void updateShouldThrowWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, requestFor(category.getId(), stockUnit.getId())))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    // ---------- delete / turnOn / turnOff ----------

    @Test
    @DisplayName("deleteById deve remover quando o produto existe")
    void deleteByIdShouldDelegateWhenExists() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        service.deleteById(id);

        verify(repository).deleteById(id);
    }

    @Test
    @DisplayName("deleteById deve lancar 404 quando o produto nao existe")
    void deleteByIdShouldThrowWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteById(id))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("turnOn deve ativar o produto")
    void turnOnShouldActivate() {
        Product product = sampleProduct();
        product.setActive(false);
        when(repository.findById(product.getId())).thenReturn(Optional.of(product));

        service.turnOn(product.getId());

        assertThat(product.isActive()).isTrue();
    }

    @Test
    @DisplayName("turnOff deve desativar o produto")
    void turnOffShouldDeactivate() {
        Product product = sampleProduct();
        when(repository.findById(product.getId())).thenReturn(Optional.of(product));

        service.turnOff(product.getId());

        assertThat(product.isActive()).isFalse();
    }

    @Test
    @DisplayName("turnOn deve lancar 404 quando o produto nao existe")
    void turnOnShouldThrowWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.turnOn(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
