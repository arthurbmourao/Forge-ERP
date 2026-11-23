package forge.erp.productunit.mapper;

import forge.erp.category.model.Category;
import forge.erp.product.model.Product;
import forge.erp.productunit.dto.ProductUnitRequest;
import forge.erp.productunit.dto.ProductUnitResponse;
import forge.erp.productunit.model.ProductUnit;
import forge.erp.unit.enums.UnitType;
import forge.erp.unit.model.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductUnitMapperTest {

    private ProductUnitMapper mapper;

    private Product product;
    private Unit box;

    @BeforeEach
    void setUp() {
        mapper = new ProductUnitMapper();

        Category category = new Category("CAT-0001", "Fixadores", "Parafusos");
        category.setId(UUID.randomUUID());

        Unit stockUnit = new Unit("UNI-0001", "Unidade", "UN", UnitType.QUANTITY, 0, true);
        stockUnit.setId(UUID.randomUUID());

        product = new Product("PRD-0001", "Parafuso M8", null, category, stockUnit, null, null, null, true);
        product.setId(UUID.randomUUID());

        box = new Unit("UNI-0002", "Caixa", "CX", UnitType.QUANTITY, 0, true);
        box.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("toEntity deve copiar os campos do request e usar product/unit resolvidos")
    void toEntityShouldMapRequestFields() {
        ProductUnitRequest request =
                new ProductUnitRequest(box.getId(), new BigDecimal("100"), true, false, true);

        ProductUnit entity = mapper.toEntity(request, product, box);

        assertThat(entity.getProduct()).isSameAs(product);
        assertThat(entity.getUnit()).isSameAs(box);
        assertThat(entity.getConversionFactor()).isEqualByComparingTo("100");
        assertThat(entity.isPurchaseUnit()).isTrue();
        assertThat(entity.isIssueUnit()).isFalse();
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    @DisplayName("toResponse deve mapear todos os campos (1 CAIXA = 100 UN)")
    void toResponseShouldMapEntityFields() {
        ProductUnit productUnit = new ProductUnit(product, box, new BigDecimal("100"), true, false, true);
        productUnit.setId(UUID.randomUUID());

        ProductUnitResponse response = mapper.toResponse(productUnit);

        assertThat(response.id()).isEqualTo(productUnit.getId());
        assertThat(response.productId()).isEqualTo(product.getId());
        assertThat(response.unitId()).isEqualTo(box.getId());
        assertThat(response.unitCode()).isEqualTo("UNI-0002");
        assertThat(response.unitSymbol()).isEqualTo("CX");
        assertThat(response.conversionFactor()).isEqualByComparingTo("100");
        assertThat(response.purchaseUnit()).isTrue();
        assertThat(response.issueUnit()).isFalse();
        assertThat(response.active()).isTrue();
    }
}
