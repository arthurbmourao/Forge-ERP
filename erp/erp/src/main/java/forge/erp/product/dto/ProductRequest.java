package forge.erp.product.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductRequest(
        @Size(max = 150)
        @NotBlank
        String name,

        @Size(max = 250)
        String description,

        @NotNull
        UUID categoryId,

        @NotNull
        UUID stockUnitId,

        @PositiveOrZero
        @Digits(integer = 13, fraction = 6)
        BigDecimal weight,

        @PositiveOrZero
        @Digits(integer = 13, fraction = 6)
        BigDecimal minimumStock,

        @PositiveOrZero
        @Digits(integer = 13, fraction = 6)
        BigDecimal maximumStock,

        @NotNull
        Boolean active
) {
}
