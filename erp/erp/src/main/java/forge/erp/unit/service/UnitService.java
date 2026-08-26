package forge.erp.unit.service;

import forge.erp.unit.DTO.UnitDtoRequest;
import forge.erp.unit.DTO.UnitDtoResponse;
import forge.erp.unit.mapper.UnitMapper;
import forge.erp.unit.model.Unit;
import forge.erp.unit.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnitService {

    private final UnitMapper mapper;
    private final UnitRepository repository;

    public UnitDtoResponse saveUnit(UnitDtoRequest request) {

        Unit unit = mapper.toEntity(request);

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
}
