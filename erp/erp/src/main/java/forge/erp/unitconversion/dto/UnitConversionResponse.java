package forge.erp.unitconversion.dto;

import forge.erp.unit.model.Unit;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UnitConversionResponse(
        UUID id,

        Unit fromUnit,

        Unit toUnit,

        BigDecimal factor,

        boolean active,

        LocalDateTime createdAt
) {
}
