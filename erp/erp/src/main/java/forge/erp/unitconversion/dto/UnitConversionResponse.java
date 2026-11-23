package forge.erp.unitconversion.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UnitConversionResponse(
        UUID id,

        UUID fromUnitId,

        String fromUnitCode,

        String fromUnitSymbol,

        UUID toUnitId,

        String toUnitCode,

        String toUnitSymbol,

        BigDecimal factor,

        boolean active,

        LocalDateTime createdAt
) {
}
