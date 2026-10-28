package forge.erp.product.mapper;

import forge.erp.category.model.Category;
import forge.erp.product.dto.ProductRequest;
import forge.erp.product.dto.ProductResponse;
import forge.erp.product.model.Product;
import forge.erp.unit.enums.UnitType;
import forge.erp.unit.model.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private ProductMapper mapper;

    private Category category;
    private Unit stockUnit;

    @BeforeEach
    void setUp() {
        mapper = new ProductMapper();

        category = new Category("CAT-0001", "Fixadores", "Parafusos e porcas");
        category.setId(UUID.randomUUID());

        stockUnit = new Unit("UNI-0001", "Unidade", "UN", UnitType.QUANTITY, 0, true);
        stockUnit.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("toEntity deve copiar os campos do request, usar category/stockUnit resolvidas e nao definir code")
    void toEntityShouldMapRequestFields() {
        ProductRequest request = new ProductRequest(
                "Parafuso Sextavado M8", "Parafuso de aço", category.getId(), stockUnit.getId(),
                new BigDecimal("0.015"), new BigDecimal("100"), new BigDecimal("1000"), true);

        Product entity = mapper.toEntity(request, category, stockUnit);

        // code e gerado no servidor, nao vem do request
        assertThat(entity.getCode()).isNull();
        assertThat(entity.getName()).isEqualTo("Parafuso Sextavado M8");
        assertThat(entity.getDescription()).isEqualTo("Parafuso de aço");
        assertThat(entity.getCategory()).isSameAs(category);
        assertThat(entity.getStockUnit()).isSameAs(stockUnit);
        assertThat(entity.getWeight()).isEqualByComparingTo("0.015");
        assertThat(entity.getMinimumStock()).isEqualByComparingTo("100");
        assertThat(entity.getMaximumStock()).isEqualByComparingTo("1000");
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    @DisplayName("toEntity deve aceitar campos opcionais nulos")
    void toEntityShouldAcceptNullOptionalFields() {
        ProductRequest request = new ProductRequest(
                "Parafuso", null, category.getId(), stockUnit.getId(), null, null, null, false);

        Product entity = mapper.toEntity(request, category, stockUnit);

        assertThat(entity.getDescription()).isNull();
        assertThat(entity.getWeight()).isNull();
        assertThat(entity.getMinimumStock()).isNull();
        assertThat(entity.getMaximumStock()).isNull();
        assertThat(entity.isActive()).isFalse();
    }

    @Test
    @DisplayName("toResponse deve mapear todos os campos e os dados de category e stockUnit")
    void toResponseShouldMapEntityFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Product product = new Product("PRD-0149", "Parafuso Sextavado M8", "Parafuso de aço",
                category, stockUnit, new BigDecimal("0.015"), new BigDecimal("100"), new BigDecimal("1000"), true);
        product.setId(id);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        ProductResponse response = mapper.toResponse(product);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.code()).isEqualTo("PRD-0149");
        assertThat(response.name()).isEqualTo("Parafuso Sextavado M8");
        assertThat(response.description()).isEqualTo("Parafuso de aço");
        assertThat(response.categoryId()).isEqualTo(category.getId());
        assertThat(response.categoryName()).isEqualTo("Fixadores");
        assertThat(response.stockUnitId()).isEqualTo(stockUnit.getId());
        assertThat(response.stockUnitSymbol()).isEqualTo("UN");
        assertThat(response.weight()).isEqualByComparingTo("0.015");
        assertThat(response.minimumStock()).isEqualByComparingTo("100");
        assertThat(response.maximumStock()).isEqualByComparingTo("1000");
        assertThat(response.active()).isTrue();
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.updatedAt()).isEqualTo(now);
    }
}
