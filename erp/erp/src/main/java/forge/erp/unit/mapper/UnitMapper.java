package forge.erp.unit.mapper;

import forge.erp.unit.dto.UnitDtoRequest;
import forge.erp.unit.dto.UnitDtoResponse;
import forge.erp.unit.model.Unit;
import org.springframework.stereotype.Component;

@Component
public class UnitMapper {

    public UnitDtoResponse toResponse(Unit unit) {

        return new UnitDtoResponse(
                unit.getId(),
                unit.getName(),
                unit.getCode(),
                unit.getSymbol(),
                unit.getType(),
                unit.getDecimalPlaces(),
                unit.isActive(),
                unit.getCreatedAt(),
                unit.getUpdatedAt()
        );
    }
    public Unit toEntity(UnitDtoRequest request){
        return new Unit(
                request.code(),
                request.name(),
                request.symbol(),
                request.type(),
                request.decimalPlaces(),
                request.active()

        );
    }
}
