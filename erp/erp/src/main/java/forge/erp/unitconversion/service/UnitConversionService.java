package forge.erp.unitconversion.service;

import forge.erp.common.exception.BusinessRuleException;
import forge.erp.common.exception.ConflictException;
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

        validateUnits(fromUnit, toUnit, null);

        UnitConversion unitConversion = mapper.toEntity(unitConversionRequest, fromUnit, toUnit);
        unitConversion = repository.save(unitConversion);

        return mapper.toResponse(unitConversion);
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
        UnitConversion unitConversion = repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Conversão de unidade", id));

        Unit fromUnit = findUnit(request.fromUnitId());
        Unit toUnit = findUnit(request.toUnitId());

        validateUnits(fromUnit, toUnit, unitConversion);

        unitConversion.setToUnit(toUnit);
        unitConversion.setFromUnit(fromUnit);
        unitConversion.setFactor(request.factor());

        repository.save(unitConversion);

        return mapper.toResponse(unitConversion);
    }

    private Unit findUnit(UUID unitId){
        return unitRepository.findById(unitId)
                .orElseThrow(() -> ResourceNotFoundException.of("Unidade", unitId));
    }

    /**
     * Valida as regras de uma conversao: unidades distintas, do mesmo UnitType (ex.: KG -> M nao
     * e permitido), ativas e sem duplicidade do par origem/destino.
     * Na edicao, uma unidade que nao mudou nao precisa estar ativa, para nao impedir ajustes
     * em conversoes cujas unidades foram inativadas depois.
     *
     * @param current conversao sendo editada (null na criacao)
     */
    private void validateUnits(Unit fromUnit, Unit toUnit, UnitConversion current){
        if (fromUnit.getId().equals(toUnit.getId())) {
            throw new BusinessRuleException("As unidades de origem e destino devem ser diferentes.");
        }

        if (fromUnit.getType() != toUnit.getType()) {
            throw new BusinessRuleException(
                    "As unidades devem pertencer ao mesmo tipo (origem: %s, destino: %s)."
                            .formatted(fromUnit.getType(), toUnit.getType()));
        }

        boolean fromChanged = current == null || !fromUnit.getId().equals(current.getFromUnit().getId());
        if (fromChanged && !fromUnit.isActive()) {
            throw new BusinessRuleException("A unidade de origem está inativa.");
        }

        boolean toChanged = current == null || !toUnit.getId().equals(current.getToUnit().getId());
        if (toChanged && !toUnit.isActive()) {
            throw new BusinessRuleException("A unidade de destino está inativa.");
        }

        UUID currentId = current == null ? null : current.getId();
        repository.findByFromUnitIdAndToUnitId(fromUnit.getId(), toUnit.getId())
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ConflictException("Já existe uma conversão entre essas unidades.");
                });
    }
}
