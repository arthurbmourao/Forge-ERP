package forge.erp.unitconversion.dto;

import forge.erp.unit.model.Unit;
import jakarta.validation.constraints.NotNull;

public record UnitConversionRequest(
        @NotNull
        Unit fromUnit,
        @NotNull
        Unit toUnit,
        @NotNull
        Integer factor,
        @NotNull
        boolean active
) {
}
