package forge.erp.unit.DTO;

import forge.erp.unit.enums.UnitType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UnitDtoRequest(
        @Size(max = 35)
        @NotNull
        String name,
        @NotNull
        String code,
        @Size(max = 5)
        @NotNull
        String symbol,
        @NotNull
        UnitType type,
        @NotNull
        Integer decimalPlaces,
        @NotNull
        boolean active
) {
}
