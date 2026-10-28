package forge.erp.unitconversion.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record UnitConversionRequest(
        @NotNull
        UUID fromUnitId,
        @NotNull
        UUID toUnitId,
        @NotNull
        BigDecimal factor,
        @NotNull
        boolean active
) {
}
