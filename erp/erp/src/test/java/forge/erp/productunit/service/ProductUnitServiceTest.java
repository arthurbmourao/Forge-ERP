package forge.erp.productunit.service;

import forge.erp.category.model.Category;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductUnitServiceTest {

    @Mock
    private ProductUnitMapper mapper;

    @Mock
    private ProductUnitRepository repository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UnitRepository unitRepository;

    @InjectMocks
    private ProductUnitService service;

    private Unit stockUnit; // UN
    private Unit box;       // CX (mesmo tipo da unidade de estoque)
    private Product product;

    @BeforeEach
    void setUp() {
        Category category = new Category("CAT-0001", "Fixadores", "Parafusos");
        category.setId(UUID.randomUUID());

        stockUnit = new Unit("UNI-0001", "Unidade", "UN", UnitType.QUANTITY, 0, true);
        stockUnit.setId(UUID.randomUUID());

        box = new Unit("UNI-0002", "Caixa", "CX", UnitType.QUANTITY, 0, true);
        box.setId(UUID.randomUUID());

        product = new Product("PRD-0001", "Parafuso M8", null, category, stockUnit, null, null, null, true);
        product.setId(UUID.randomUUID());
    }

    private ProductUnitRequest requestFor(Unit unit) {
        return new ProductUnitRequest(unit.getId(), new BigDecimal("100"), true, true, true);
    }

    private ProductUnit existing(Unit unit) {
        ProductUnit productUnit = new ProductUnit(product, unit, new BigDecimal("50"), false, false, true);
        productUnit.setId(UUID.randomUUID());
        return productUnit;
    }

    private ProductUnitResponse responseOf(ProductUnit pu) {
        return new ProductUnitResponse(pu.getId(), pu.getProduct().getId(), pu.getUnit().getId(),
                pu.getUnit().getCode(), pu.getUnit().getSymbol(), pu.getConversionFactor(),
                pu.isPurchaseUnit(), pu.isIssueUnit(), pu.isActive());
    }

    // ---------- save ----------

    @Test
    @DisplayName("save deve persistir o vinculo (1 CAIXA = 100 UN) e retornar a resposta")
    void saveShouldPersistAndReturnResponse() {
        ProductUnitRequest request = requestFor(box);
        ProductUnit entity = new ProductUnit(product, box, new BigDecimal("100"), true, true, true);
        ProductUnitResponse expected = responseOf(entity);

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(unitRepository.findById(box.getId())).thenReturn(Optional.of(box));
        when(repository.findByProductIdAndUnitId(product.getId(), box.getId())).thenReturn(Optional.empty());
        when(mapper.toEntity(request, product, box)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(expected);

        ProductUnitResponse result = service.save(product.getId(), request);

        assertThat(result).isEqualTo(expected);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("save deve lancar 404 quando o produto nao existe")
    void saveShouldFailWhenProductMissing() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.save(product.getId(), requestFor(box)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("save deve rejeitar produto inativo")
    void saveShouldRejectInactiveProduct() {
        product.setActive(false);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> service.save(product.getId(), requestFor(box)))
                .isInstanceOf(BusinessRuleException.class);
        verifyNoInteractions(unitRepository);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("save deve lancar 404 quando a unidade nao existe")
    void saveShouldFailWhenUnitMissing() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(unitRepository.findById(box.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.save(product.getId(), requestFor(box)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("save deve rejeitar unidade inativa")
    void saveShouldRejectInactiveUnit() {
        box.setActive(false);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(unitRepository.findById(box.getId())).thenReturn(Optional.of(box));

        assertThatThrownBy(() -> service.save(product.getId(), requestFor(box)))
                .isInstanceOf(BusinessRuleException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("save deve rejeitar a propria unidade de estoque do produto")
    void saveShouldRejectStockUnit() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(unitRepository.findById(stockUnit.getId())).thenReturn(Optional.of(stockUnit));

        assertThatThrownBy(() -> service.save(product.getId(), requestFor(stockUnit)))
                .isInstanceOf(BusinessRuleException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("save deve rejeitar unidade de tipo diferente da unidade de estoque")
    void saveShouldRejectDifferentUnitType() {
        Unit kilo = new Unit("UNI-0003", "Quilo", "kg", UnitType.MASS, 3, true);
        kilo.setId(UUID.randomUUID());
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(unitRepository.findById(kilo.getId())).thenReturn(Optional.of(kilo));

        assertThatThrownBy(() -> service.save(product.getId(), requestFor(kilo)))
                .isInstanceOf(BusinessRuleException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("save deve lancar conflito quando a unidade ja esta vinculada ao produto")
    void saveShouldRejectDuplicate() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(unitRepository.findById(box.getId())).thenReturn(Optional.of(box));
        when(repository.findByProductIdAndUnitId(product.getId(), box.getId()))
                .thenReturn(Optional.of(existing(box)));

        assertThatThrownBy(() -> service.save(product.getId(), requestFor(box)))
                .isInstanceOf(ConflictException.class);
        verify(repository, never()).save(any());
    }

    // ---------- findAll / findById ----------

    @Test
    @DisplayName("findAll deve mapear os vinculos do produto")
    void findAllShouldMapAll() {
        ProductUnit productUnit = existing(box);
        ProductUnitResponse expected = responseOf(productUnit);
        when(productRepository.existsById(product.getId())).thenReturn(true);
        when(repository.findAllByProductId(product.getId())).thenReturn(List.of(productUnit));
        when(mapper.toResponse(productUnit)).thenReturn(expected);

        assertThat(service.findAll(product.getId())).containsExactly(expected);
    }

    @Test
    @DisplayName("findAll deve lancar 404 quando o produto nao existe")
    void findAllShouldFailWhenProductMissing() {
        when(productRepository.existsById(product.getId())).thenReturn(false);

        assertThatThrownBy(() -> service.findAll(product.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("findById deve retornar o vinculo do produto")
    void findByIdShouldReturnExisting() {
        ProductUnit productUnit = existing(box);
        ProductUnitResponse expected = responseOf(productUnit);
        when(repository.findByIdAndProductId(productUnit.getId(), product.getId()))
                .thenReturn(Optional.of(productUnit));
        when(mapper.toResponse(productUnit)).thenReturn(expected);

        assertThat(service.findById(product.getId(), productUnit.getId())).isEqualTo(expected);
    }

    @Test
    @DisplayName("findById deve lancar 404 quando o vinculo nao pertence ao produto")
    void findByIdShouldThrowWhenNotOfProduct() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndProductId(id, product.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(product.getId(), id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------- update ----------

    @Test
    @DisplayName("update deve aplicar os valores do request")
    void updateShouldApplyRequestValues() {
        ProductUnit productUnit = existing(box);
        ProductUnitRequest request = new ProductUnitRequest(box.getId(), new BigDecimal("200"), true, true, false);

        when(repository.findByIdAndProductId(productUnit.getId(), product.getId()))
                .thenReturn(Optional.of(productUnit));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(unitRepository.findById(box.getId())).thenReturn(Optional.of(box));
        // o unico vinculo encontrado e o proprio registro: nao e duplicata
        when(repository.findByProductIdAndUnitId(product.getId(), box.getId()))
                .thenReturn(Optional.of(productUnit));
        when(repository.save(productUnit)).thenReturn(productUnit);
        when(mapper.toResponse(productUnit)).thenReturn(responseOf(productUnit));

        service.update(product.getId(), productUnit.getId(), request);

        assertThat(productUnit.getConversionFactor()).isEqualByComparingTo("200");
        assertThat(productUnit.isPurchaseUnit()).isTrue();
        assertThat(productUnit.isIssueUnit()).isTrue();
        assertThat(productUnit.isActive()).isFalse();
        verify(repository).save(productUnit);
    }

    @Test
    @DisplayName("update nao deve exigir unidade ativa quando a unidade nao mudou")
    void updateShouldAllowUnchangedInactiveUnit() {
        box.setActive(false);
        ProductUnit productUnit = existing(box);

        when(repository.findByIdAndProductId(productUnit.getId(), product.getId()))
                .thenReturn(Optional.of(productUnit));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(unitRepository.findById(box.getId())).thenReturn(Optional.of(box));
        when(repository.findByProductIdAndUnitId(product.getId(), box.getId()))
                .thenReturn(Optional.of(productUnit));
        when(repository.save(productUnit)).thenReturn(productUnit);
        when(mapper.toResponse(productUnit)).thenReturn(responseOf(productUnit));

        service.update(product.getId(), productUnit.getId(), requestFor(box));

        verify(repository).save(productUnit);
    }

    @Test
    @DisplayName("update deve lancar conflito ao trocar para uma unidade ja vinculada a outro registro")
    void updateShouldRejectDuplicateWithOtherRecord() {
        Unit pallet = new Unit("UNI-0004", "Palete", "PL", UnitType.QUANTITY, 0, true);
        pallet.setId(UUID.randomUUID());
        ProductUnit productUnit = existing(box);
        ProductUnit other = existing(pallet);

        when(repository.findByIdAndProductId(productUnit.getId(), product.getId()))
                .thenReturn(Optional.of(productUnit));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(unitRepository.findById(pallet.getId())).thenReturn(Optional.of(pallet));
        when(repository.findByProductIdAndUnitId(product.getId(), pallet.getId()))
                .thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.update(product.getId(), productUnit.getId(), requestFor(pallet)))
                .isInstanceOf(ConflictException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update deve lancar 404 quando o vinculo nao existe")
    void updateShouldThrowWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndProductId(id, product.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(product.getId(), id, requestFor(box)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    // ---------- delete / turnOn / turnOff ----------

    @Test
    @DisplayName("deleteById deve remover o vinculo do produto")
    void deleteByIdShouldDelete() {
        ProductUnit productUnit = existing(box);
        when(repository.findByIdAndProductId(productUnit.getId(), product.getId()))
                .thenReturn(Optional.of(productUnit));

        service.deleteById(product.getId(), productUnit.getId());

        verify(repository).delete(productUnit);
    }

    @Test
    @DisplayName("deleteById deve lancar 404 quando o vinculo nao existe")
    void deleteByIdShouldThrowWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndProductId(id, product.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteById(product.getId(), id))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("turnOn deve ativar o vinculo")
    void turnOnShouldActivate() {
        ProductUnit productUnit = existing(box);
        productUnit.setActive(false);
        when(repository.findByIdAndProductId(productUnit.getId(), product.getId()))
                .thenReturn(Optional.of(productUnit));

        service.turnOn(product.getId(), productUnit.getId());

        assertThat(productUnit.isActive()).isTrue();
    }

    @Test
    @DisplayName("turnOff deve desativar o vinculo")
    void turnOffShouldDeactivate() {
        ProductUnit productUnit = existing(box);
        when(repository.findByIdAndProductId(productUnit.getId(), product.getId()))
                .thenReturn(Optional.of(productUnit));

        service.turnOff(product.getId(), productUnit.getId());

        assertThat(productUnit.isActive()).isFalse();
    }
}
