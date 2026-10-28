package forge.erp.productunit.mapper;

import forge.erp.product.model.Product;
import forge.erp.productunit.dto.ProductUnitRequest;
import forge.erp.productunit.dto.ProductUnitResponse;
import forge.erp.productunit.model.ProductUnit;
import forge.erp.unit.model.Unit;
import org.springframework.stereotype.Component;

@Component
public class ProductUnitMapper {

    public ProductUnitResponse toResponse(ProductUnit productUnit) {
        return new ProductUnitResponse(
                productUnit.getId(),
                productUnit.getProduct().getId(),
                productUnit.getUnit().getId(),
                productUnit.getUnit().getCode(),
                productUnit.getUnit().getSymbol(),
                productUnit.getConversionFactor(),
                productUnit.isPurchaseUnit(),
                productUnit.isIssueUnit(),
                productUnit.isActive()
        );
    }

    public ProductUnit toEntity(ProductUnitRequest request, Product product, Unit unit) {
        return new ProductUnit(
                product,
                unit,
                request.conversionFactor(),
                request.purchaseUnit(),
                request.issueUnit(),
                request.active()
        );
    }
}
