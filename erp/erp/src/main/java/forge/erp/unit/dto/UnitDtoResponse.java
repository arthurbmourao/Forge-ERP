package forge.erp.unit.dto;

import forge.erp.unit.enums.UnitType;

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
