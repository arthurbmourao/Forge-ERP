package forge.erp.unitconversion.mapper;

import forge.erp.unit.model.Unit;
import forge.erp.unitconversion.dto.UnitConversionRequest;
import forge.erp.unitconversion.dto.UnitConversionResponse;
import forge.erp.unitconversion.model.UnitConversion;
import org.springframework.stereotype.Component;

@Component
public class UnitConversionMapper {

    public UnitConversionResponse toResponse(UnitConversion unitConversion){
        Unit fromUnit = unitConversion.getFromUnit();
        Unit toUnit = unitConversion.getToUnit();

        return new UnitConversionResponse(
                unitConversion.getId(),
                fromUnit.getId(),
                fromUnit.getCode(),
                fromUnit.getSymbol(),
                toUnit.getId(),
                toUnit.getCode(),
                toUnit.getSymbol(),
                unitConversion.getFactor(),
                unitConversion.isActive(),
                unitConversion.getCreatedAt()
        );
    }

    public UnitConversion toEntity (UnitConversionRequest unitConversionRequest, Unit fromUnit, Unit toUnit){
        return new UnitConversion(
                fromUnit,
                toUnit,
                unitConversionRequest.factor(),
                unitConversionRequest.active()
        );
    }
}
