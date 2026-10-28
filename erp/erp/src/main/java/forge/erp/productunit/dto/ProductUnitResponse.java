package forge.erp.productunit.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductUnitResponse(
        UUID id,

        UUID productId,

        UUID unitId,

        String unitCode,

        String unitSymbol,

        BigDecimal conversionFactor,

        boolean purchaseUnit,

        boolean issueUnit,

        boolean active
) {
}
