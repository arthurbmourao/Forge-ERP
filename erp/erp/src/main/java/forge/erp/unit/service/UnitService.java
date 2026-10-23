package forge.erp.unit.service;

import forge.erp.common.code.CodeGenerator;
import forge.erp.unit.dto.UnitDtoRequest;
import forge.erp.unit.dto.UnitDtoResponse;
import forge.erp.unit.mapper.UnitMapper;
import forge.erp.unit.model.Unit;
import forge.erp.unit.repository.UnitRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UnitService {

    private static final String CODE_PREFIX = "UNI";

    private final UnitMapper mapper;
    private final UnitRepository repository;
    private final CodeGenerator codeGenerator;

    public UnitDtoResponse saveUnit(UnitDtoRequest request) {

        Unit unit = mapper.toEntity(request);

        unit.setCode(codeGenerator.generate(CODE_PREFIX, repository::existsByCode));

        unit = repository.save(unit);

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

    public List<UnitDtoResponse> getAll(){
        return repository.findAll().stream().map(
                mapper :: toResponse
        ).toList();
    }

    public UnitDtoResponse getById(UUID id){
        Unit unit = repository.findById(id).orElseThrow(RuntimeException::new);

        return mapper.toResponse(unit);
    }

    public void deleteById(UUID id){
        repository.deleteById(id);
    }

    @Transactional
    public void turnOff(UUID id){
        Unit unit = repository.findById(id).orElseThrow(RuntimeException::new);
        unit.setActive(false);

    }

    @Transactional
    public void turnOn(UUID id){
        Unit unit = repository.findById(id).orElseThrow(RuntimeException::new);
        unit.setActive(true);

    }


}
