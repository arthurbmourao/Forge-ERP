package forge.erp.unitconversion.dto;

import forge.erp.unit.model.Unit;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UnitConversionRequest(
        @NotNull
        Unit fromUnit,
        @NotNull
        Unit toUnit,
        @NotNull
        BigDecimal factor,
        @NotNull
        boolean active
) {
}
