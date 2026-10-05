package forge.erp.unitconversion.service;

import forge.erp.unitconversion.dto.UnitConversionRequest;
import forge.erp.unitconversion.dto.UnitConversionResponse;
import forge.erp.unitconversion.mapper.UnitConversionMapper;
import forge.erp.unitconversion.model.UnitConversion;
import forge.erp.unitconversion.repository.UnitConversionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UnitConversionService {

    private final UnitConversionRepository repository;
    private final UnitConversionMapper mapper;

    public UnitConversionResponse saveUnitConversion (UnitConversionRequest unitConversionRequest){
        UnitConversion unitConversion = mapper.toEntity(unitConversionRequest);
        unitConversion = repository.save(unitConversion);

        return new UnitConversionResponse(
                unitConversion.getId(),
                unitConversion.getFromUnit(),
                unitConversion.getToUnit(),
                unitConversion.getFactor(),
                unitConversion.isActive(),
                unitConversion.getCreatedAt()
        );
    }

    public UnitConversionResponse findById (UUID id){
        UnitConversion unitConversion = repository.findById(id).orElseThrow();

        UnitConversionResponse unitConversionResponse = mapper.toResponse(unitConversion);

        return unitConversionResponse;

    }

    public List<UnitConversionResponse> findAll (){
        List<UnitConversion> units = repository.findAll();

        List<UnitConversionResponse> unitResponse = units.stream().map(mapper::toResponse).toList();

        return unitResponse;
    }

    public void deleteById(UUID id){
        repository.deleteById(id);
    }

    @Transactional
    public void turnOn(UUID id){
        UnitConversion unitConversion = repository.findById(id).orElseThrow();

        unitConversion.setActive(true);
    }

    @Transactional
    public void turnOff(UUID id){
        UnitConversion unitConversion = repository.findById(id).orElseThrow();

        unitConversion.setActive(false);
    }
}
