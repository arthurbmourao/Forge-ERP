package forge.erp.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponse(
        UUID id,

        String code,

        String name,

        String description,

        UUID categoryId,

        String categoryName,

        UUID stockUnitId,

        String stockUnitSymbol,

        BigDecimal weight,

        BigDecimal minimumStock,

        BigDecimal maximumStock,

        boolean active,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
