package forge.erp.unit.service;

import forge.erp.unit.dto.UnitDtoRequest;
import forge.erp.unit.dto.UnitDtoResponse;
import forge.erp.unit.enums.UnitType;
import forge.erp.unit.mapper.UnitMapper;
import forge.erp.unit.model.Unit;
import forge.erp.unit.repository.UnitRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnitServiceTest {

    @Mock
    private UnitMapper mapper;

    @Mock
    private UnitRepository repository;

    @InjectMocks
    private UnitService service;

    private Unit sampleEntity() {
        Unit unit = new Unit("KG", "Quilograma", "kg", UnitType.MASS, 3, true);
        unit.setId(UUID.randomUUID());
        unit.setCreatedAt(LocalDateTime.now());
        unit.setUpdatedAt(LocalDateTime.now());
        return unit;
    }

    private UnitDtoResponse responseOf(Unit u) {
        return new UnitDtoResponse(u.getId(), u.getName(), u.getCode(), u.getSymbol(),
                u.getType(), u.getDecimalPlaces(), u.isActive(), u.getCreatedAt(), u.getUpdatedAt());
    }

    @Test
    @DisplayName("saveUnit deve persistir e retornar a resposta construida com os dados da entidade")
    void saveUnitShouldPersistAndReturnResponse() {
        UnitDtoRequest request = new UnitDtoRequest("Quilograma", "KG", "kg", UnitType.MASS, 3, true);
        Unit entity = sampleEntity();

        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);

        UnitDtoResponse result = service.saveUnit(request);

        assertThat(result.id()).isEqualTo(entity.getId());
        assertThat(result.code()).isEqualTo("KG");
        assertThat(result.name()).isEqualTo("Quilograma");
        assertThat(result.type()).isEqualTo(UnitType.MASS);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("getAll deve mapear todas as unidades")
    void getAllShouldMapAll() {
        Unit entity = sampleEntity();
        UnitDtoResponse expected = responseOf(entity);

        when(repository.findAll()).thenReturn(List.of(entity));
        when(mapper.toResponse(entity)).thenReturn(expected);

        List<UnitDtoResponse> result = service.getAll();

        assertThat(result).containsExactly(expected);
    }

    @Test
    @DisplayName("getById deve retornar unidade existente")
    void getByIdShouldReturnExisting() {
        Unit entity = sampleEntity();
        UnitDtoResponse expected = responseOf(entity);

        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(expected);

        UnitDtoResponse result = service.getById(entity.getId());

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("getById deve lancar excecao quando nao encontrado")
    void getByIdShouldThrowWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("deleteById deve delegar para o repositorio")
    void deleteByIdShouldDelegate() {
        UUID id = UUID.randomUUID();

        service.deleteById(id);

        verify(repository).deleteById(id);
    }

    @Test
    @DisplayName("turnOn deve ativar a unidade")
    void turnOnShouldActivate() {
        Unit entity = sampleEntity();
        entity.setActive(false);
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

        service.turnOn(entity.getId());

        assertThat(entity.isActive()).isTrue();
    }

    @Test
    @DisplayName("turnOff deve desativar a unidade")
    void turnOffShouldDeactivate() {
        Unit entity = sampleEntity();
        entity.setActive(true);
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

        service.turnOff(entity.getId());

        assertThat(entity.isActive()).isFalse();
    }
}
