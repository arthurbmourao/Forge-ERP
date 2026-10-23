package forge.erp.unitconversion.service;

import forge.erp.unit.enums.UnitType;
import forge.erp.unit.model.Unit;
import forge.erp.unitconversion.dto.UnitConversionRequest;
import forge.erp.unitconversion.dto.UnitConversionResponse;
import forge.erp.unitconversion.mapper.UnitConversionMapper;
import forge.erp.unitconversion.model.UnitConversion;
import forge.erp.unitconversion.repository.UnitConversionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnitConversionServiceTest {

    @Mock
    private UnitConversionRepository repository;

    @Mock
    private UnitConversionMapper mapper;

    @InjectMocks
    private UnitConversionService service;

    private Unit unit(String code, String name, String symbol) {
        Unit unit = new Unit(code, name, symbol, UnitType.MASS, 3, true);
        unit.setId(UUID.randomUUID());
        return unit;
    }

    private UnitConversion sampleEntity() {
        UnitConversion conversion = new UnitConversion();
        conversion.setId(UUID.randomUUID());
        conversion.setFromUnit(unit("KG", "Quilograma", "kg"));
        conversion.setToUnit(unit("G", "Grama", "g"));
        conversion.setFactor(new BigDecimal("1000"));
        conversion.setActive(true);
        conversion.setCreatedAt(LocalDateTime.now());
        return conversion;
    }

    private UnitConversionResponse responseOf(UnitConversion c) {
        return new UnitConversionResponse(c.getId(), c.getFromUnit(), c.getToUnit(),
                c.getFactor(), c.isActive(), c.getCreatedAt());
    }

    @Test
    @DisplayName("saveUnitConversion deve persistir e retornar resposta com os dados da entidade")
    void saveShouldPersistAndReturnResponse() {
        UnitConversion entity = sampleEntity();
        UnitConversionRequest request = new UnitConversionRequest(
                entity.getFromUnit(), entity.getToUnit(), entity.getFactor(), true);

        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);

        UnitConversionResponse result = service.saveUnitConversion(request);

        assertThat(result.id()).isEqualTo(entity.getId());
        assertThat(result.fromUnit()).isEqualTo(entity.getFromUnit());
        assertThat(result.toUnit()).isEqualTo(entity.getToUnit());
        assertThat(result.factor()).isEqualByComparingTo("1000");
        assertThat(result.active()).isTrue();
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("findById deve retornar conversao existente")
    void findByIdShouldReturnExisting() {
        UnitConversion entity = sampleEntity();
        UnitConversionResponse expected = responseOf(entity);

        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(expected);

        UnitConversionResponse result = service.findById(entity.getId());

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("findById deve lancar excecao quando nao encontrado")
    void findByIdShouldThrowWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("findAll deve mapear todas as conversoes")
    void findAllShouldMapAll() {
        UnitConversion entity = sampleEntity();
        UnitConversionResponse expected = responseOf(entity);

        when(repository.findAll()).thenReturn(List.of(entity));
        when(mapper.toResponse(entity)).thenReturn(expected);

        List<UnitConversionResponse> result = service.findAll();

        assertThat(result).containsExactly(expected);
    }

    @Test
    @DisplayName("deleteById deve delegar para o repositorio")
    void deleteByIdShouldDelegate() {
        UUID id = UUID.randomUUID();

        service.deleteById(id);

        verify(repository).deleteById(id);
    }

    @Test
    @DisplayName("turnOn deve ativar a conversao")
    void turnOnShouldActivate() {
        UnitConversion entity = sampleEntity();
        entity.setActive(false);
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

        service.turnOn(entity.getId());

        assertThat(entity.isActive()).isTrue();
    }

    @Test
    @DisplayName("turnOff deve desativar a conversao")
    void turnOffShouldDeactivate() {
        UnitConversion entity = sampleEntity();
        entity.setActive(true);
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

        service.turnOff(entity.getId());

        assertThat(entity.isActive()).isFalse();
    }
}
