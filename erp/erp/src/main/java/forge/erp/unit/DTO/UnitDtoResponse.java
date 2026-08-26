package forge.erp.unit.DTO;

import forge.erp.unit.enums.UnitType;
import forge.erp.unit.model.Unit;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record UnitDtoResponse(
        UUID id,
        String name,
        String code,
        String symbol,
        UnitType type,
        Integer decimalPlaces,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updateAt
) {

}
