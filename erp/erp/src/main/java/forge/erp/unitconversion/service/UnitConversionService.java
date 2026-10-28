package forge.erp.unitconversion.service;

import forge.erp.common.exception.ResourceNotFoundException;
import forge.erp.unit.model.Unit;
import forge.erp.unit.repository.UnitRepository;
import forge.erp.unitconversion.dto.UnitConversionRequest;
import forge.erp.unitconversion.dto.UnitConversionResponse;
import forge.erp.unitconversion.mapper.UnitConversionMapper;
import forge.erp.unitconversion.model.UnitConversion;
import forge.erp.unitconversion.repository.UnitConversionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UnitConversionService {

    private final UnitConversionRepository repository;
    private final UnitConversionMapper mapper;
    private final UnitRepository unitRepository;

    public UnitConversionResponse saveUnitConversion (UnitConversionRequest unitConversionRequest){
        Unit fromUnit = findUnit(unitConversionRequest.fromUnitId());
        Unit toUnit = findUnit(unitConversionRequest.toUnitId());

        UnitConversion unitConversion = mapper.toEntity(unitConversionRequest, fromUnit, toUnit);
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
        UnitConversion unitConversion = repository.findById(id).orElseThrow(RuntimeException::new);

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
        UnitConversion unitConversion = repository.findById(id).orElseThrow(RuntimeException::new);

        unitConversion.setActive(true);
    }

    @Transactional
    public void turnOff(UUID id){
        UnitConversion unitConversion = repository.findById(id).orElseThrow(RuntimeException::new);

        unitConversion.setActive(false);
    }

    public UnitConversionResponse update(UnitConversionRequest request, UUID id){
        UnitConversion unitConversion = repository.findById(id).orElseThrow();

        unitConversion.setToUnit(findUnit(request.toUnitId()));
        unitConversion.setFromUnit(findUnit(request.fromUnitId()));
        unitConversion.setFactor(request.factor());

        repository.save(unitConversion);

        return mapper.toResponse(unitConversion);
    }

    private Unit findUnit(UUID unitId){
        return unitRepository.findById(unitId)
                .orElseThrow(() -> ResourceNotFoundException.of("Unidade", unitId));
    }
}
