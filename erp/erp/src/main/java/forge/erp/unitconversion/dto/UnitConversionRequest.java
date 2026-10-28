package forge.erp.unitconversion.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record UnitConversionRequest(
        @NotNull
        UUID fromUnitId,
        @NotNull
        UUID toUnitId,
        @NotNull
        @Positive
        @Digits(integer = 13, fraction = 6)
        BigDecimal factor,
        @NotNull
        boolean active
) {
}
