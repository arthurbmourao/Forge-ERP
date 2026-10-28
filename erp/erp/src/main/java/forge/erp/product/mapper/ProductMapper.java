package forge.erp.product.mapper;

import forge.erp.category.model.Category;
import forge.erp.product.dto.ProductRequest;
import forge.erp.product.dto.ProductResponse;
import forge.erp.product.model.Product;
import forge.erp.unit.model.Unit;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getDescription(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getStockUnit().getId(),
                product.getStockUnit().getSymbol(),
                product.getWeight(),
                product.getMinimumStock(),
                product.getMaximumStock(),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public Product toEntity(ProductRequest request, Category category, Unit stockUnit) {
        // code e gerado no servidor (ProductService), nao vem do request
        return new Product(
                null,
                request.name(),
                request.description(),
                category,
                stockUnit,
                request.weight(),
                request.minimumStock(),
                request.maximumStock(),
                request.active()
        );
    }
}
