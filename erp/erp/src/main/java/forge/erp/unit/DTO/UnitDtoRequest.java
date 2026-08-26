package forge.erp.unit.DTO;

import forge.erp.unit.enums.UnitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UnitDtoRequest(
        @Size(max = 35)
        @NotBlank
        String name,
        @NotBlank
        String code,
        @Size(max = 5)
        @NotBlank
        String symbol,
        @NotNull
        UnitType type,
        @NotNull
        Integer decimalPlaces,
        @NotNull
        boolean active
) {
}
