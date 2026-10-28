package forge.erp.productunit.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductUnitRequest(
        @NotNull
        UUID unitId,

        @NotNull
        @Positive
        @Digits(integer = 13, fraction = 6)
        BigDecimal conversionFactor,

        @NotNull
        Boolean purchaseUnit,

        @NotNull
        Boolean issueUnit,

        @NotNull
        Boolean active
) {
}
