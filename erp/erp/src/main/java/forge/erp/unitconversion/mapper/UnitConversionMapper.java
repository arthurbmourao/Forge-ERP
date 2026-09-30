package forge.erp.unitconversion.mapper;

import forge.erp.unit.model.Unit;
import forge.erp.unitconversion.dto.UnitConversionRequest;
import forge.erp.unitconversion.dto.UnitConversionResponse;
import forge.erp.unitconversion.model.UnitConversion;
import org.springframework.stereotype.Component;

@Component
public class UnitConversionMapper {

    public UnitConversionResponse toResponse(UnitConversion unitConversion){
        return new UnitConversionResponse(
                unitConversion.getId(),
                unitConversion.getFromUnit(),
                unitConversion.getToUnit(),
                unitConversion.getFactor(),
                unitConversion.isActive(),
                unitConversion.getCreatedAt()
        );
    }

    public UnitConversion toEntity (UnitConversionRequest unitConversionRequest){
        return new UnitConversion(
                unitConversionRequest.fromUnit(),
                unitConversionRequest.toUnit(),
                unitConversionRequest.factor(),
                unitConversionRequest.active()
        );
    }
}
