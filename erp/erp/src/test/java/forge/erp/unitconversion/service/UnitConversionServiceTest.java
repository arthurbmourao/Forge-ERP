package forge.erp.unitconversion.service;

import forge.erp.common.exception.BusinessRuleException;
import forge.erp.common.exception.ConflictException;
import forge.erp.common.exception.ResourceNotFoundException;
import forge.erp.unit.enums.UnitType;
import forge.erp.unit.model.Unit;
import forge.erp.unit.repository.UnitRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnitConversionServiceTest {

    @Mock
    private UnitConversionRepository repository;

    @Mock
    private UnitConversionMapper mapper;

    @Mock
    private UnitRepository unitRepository;

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

    private Unit unit(String code, UnitType type, boolean active) {
        Unit unit = new Unit(code, code, code.toLowerCase(), type, 0, active);
        unit.setId(UUID.randomUUID());
        return unit;
    }

    private UnitConversionResponse responseOf(UnitConversion c) {
        return new UnitConversionResponse(c.getId(),
                c.getFromUnit().getId(), c.getFromUnit().getCode(), c.getFromUnit().getSymbol(),
                c.getToUnit().getId(), c.getToUnit().getCode(), c.getToUnit().getSymbol(),
                c.getFactor(), c.isActive(), c.getCreatedAt());
    }

    private UnitConversionRequest requestFor(Unit from, Unit to) {
        return new UnitConversionRequest(from.getId(), to.getId(), new BigDecimal("1000"), true);
    }

    private void stubUnits(Unit... units) {
        for (Unit u : units) {
            when(unitRepository.findById(u.getId())).thenReturn(Optional.of(u));
        }
    }

    @Test
    @DisplayName("saveUnitConversion deve persistir e retornar resposta com os dados da entidade")
    void saveShouldPersistAndReturnResponse() {
        UnitConversion entity = sampleEntity();
        Unit from = entity.getFromUnit();
        Unit to = entity.getToUnit();
        UnitConversionRequest request = new UnitConversionRequest(
                from.getId(), to.getId(), entity.getFactor(), true);

        when(unitRepository.findById(from.getId())).thenReturn(Optional.of(from));
        when(unitRepository.findById(to.getId())).thenReturn(Optional.of(to));
        when(mapper.toEntity(request, from, to)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(responseOf(entity));

        UnitConversionResponse result = service.saveUnitConversion(request);

        assertThat(result.id()).isEqualTo(entity.getId());
        assertThat(result.fromUnitId()).isEqualTo(from.getId());
        assertThat(result.toUnitId()).isEqualTo(to.getId());
        assertThat(result.factor()).isEqualByComparingTo("1000");
        assertThat(result.active()).isTrue();
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("saveUnitConversion deve lancar 404 e nao salvar quando a unidade de origem nao existe")
    void saveShouldFailWhenFromUnitMissing() {
        UnitConversionRequest request = new UnitConversionRequest(
                UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("1000"), true);
        when(unitRepository.findById(request.fromUnitId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.saveUnitConversion(request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("saveUnitConversion deve lancar 404 e nao salvar quando a unidade de destino nao existe")
    void saveShouldFailWhenToUnitMissing() {
        Unit from = unit("KG", "Quilograma", "kg");
        UnitConversionRequest request = new UnitConversionRequest(
                from.getId(), UUID.randomUUID(), new BigDecimal("1000"), true);
        when(unitRepository.findById(from.getId())).thenReturn(Optional.of(from));
        when(unitRepository.findById(request.toUnitId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.saveUnitConversion(request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update deve aplicar unidades resolvidas por id e o fator do request")
    void updateShouldApplyResolvedUnitsAndFactor() {
        UnitConversion entity = sampleEntity();
        Unit newFrom = unit("L", "Litro", "l");
        Unit newTo = unit("ML", "Mililitro", "ml");
        UnitConversionRequest request = new UnitConversionRequest(
                newFrom.getId(), newTo.getId(), new BigDecimal("500"), true);

        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(unitRepository.findById(newFrom.getId())).thenReturn(Optional.of(newFrom));
        when(unitRepository.findById(newTo.getId())).thenReturn(Optional.of(newTo));
        when(mapper.toResponse(entity)).thenReturn(responseOf(entity));

        service.update(request, entity.getId());

        assertThat(entity.getFromUnit()).isSameAs(newFrom);
        assertThat(entity.getToUnit()).isSameAs(newTo);
        assertThat(entity.getFactor()).isEqualByComparingTo("500");
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("update deve lancar 404 e nao salvar quando uma das unidades nao existe")
    void updateShouldFailWhenUnitMissing() {
        UnitConversion entity = sampleEntity();
        UnitConversionRequest request = new UnitConversionRequest(
                UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("500"), true);

        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(unitRepository.findById(request.fromUnitId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(request, entity.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update deve lancar 404 quando a conversao nao existe")
    void updateShouldFailWhenConversionMissing() {
        UUID id = UUID.randomUUID();
        UnitConversionRequest request = new UnitConversionRequest(
                UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("500"), true);
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(request, id))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    // ---------- regras da conversao ----------

    @Test
    @DisplayName("save deve rejeitar a mesma unidade como origem e destino")
    void saveShouldRejectSameUnit() {
        Unit kg = unit("KG", UnitType.MASS, true);
        stubUnits(kg);

        assertThatThrownBy(() -> service.saveUnitConversion(requestFor(kg, kg)))
                .isInstanceOf(BusinessRuleException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("save deve rejeitar unidades de tipos diferentes (KG -> M)")
    void saveShouldRejectDifferentUnitTypes() {
        Unit kg = unit("KG", UnitType.MASS, true);
        Unit m = unit("M", UnitType.LENGTH, true);
        stubUnits(kg, m);

        assertThatThrownBy(() -> service.saveUnitConversion(requestFor(kg, m)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("MASS")
                .hasMessageContaining("LENGTH");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("save deve rejeitar unidade de origem inativa")
    void saveShouldRejectInactiveFromUnit() {
        Unit kg = unit("KG", UnitType.MASS, false);
        Unit g = unit("G", UnitType.MASS, true);
        stubUnits(kg, g);

        assertThatThrownBy(() -> service.saveUnitConversion(requestFor(kg, g)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("origem");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("save deve rejeitar unidade de destino inativa")
    void saveShouldRejectInactiveToUnit() {
        Unit kg = unit("KG", UnitType.MASS, true);
        Unit g = unit("G", UnitType.MASS, false);
        stubUnits(kg, g);

        assertThatThrownBy(() -> service.saveUnitConversion(requestFor(kg, g)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("destino");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("save deve lancar conflito quando ja existe conversao para o par origem/destino")
    void saveShouldRejectDuplicatePair() {
        Unit kg = unit("KG", UnitType.MASS, true);
        Unit g = unit("G", UnitType.MASS, true);
        stubUnits(kg, g);
        UnitConversion existing = new UnitConversion(kg, g, new BigDecimal("1000"), true);
        existing.setId(UUID.randomUUID());
        when(repository.findByFromUnitIdAndToUnitId(kg.getId(), g.getId())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.saveUnitConversion(requestFor(kg, g)))
                .isInstanceOf(ConflictException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update nao deve tratar o proprio registro como duplicata")
    void updateShouldNotTreatItselfAsDuplicate() {
        UnitConversion entity = sampleEntity();
        Unit from = entity.getFromUnit();
        Unit to = entity.getToUnit();
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        stubUnits(from, to);
        when(repository.findByFromUnitIdAndToUnitId(from.getId(), to.getId())).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(responseOf(entity));

        service.update(new UnitConversionRequest(from.getId(), to.getId(), new BigDecimal("2000"), true),
                entity.getId());

        assertThat(entity.getFactor()).isEqualByComparingTo("2000");
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("update deve lancar conflito ao trocar para um par que ja pertence a outra conversao")
    void updateShouldRejectDuplicateWithOtherConversion() {
        UnitConversion entity = sampleEntity();
        Unit from = entity.getFromUnit();
        Unit newTo = unit("MG", UnitType.MASS, true);
        UnitConversion other = new UnitConversion(from, newTo, new BigDecimal("1000000"), true);
        other.setId(UUID.randomUUID());
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        stubUnits(from, newTo);
        when(repository.findByFromUnitIdAndToUnitId(from.getId(), newTo.getId())).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.update(requestFor(from, newTo), entity.getId()))
                .isInstanceOf(ConflictException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update nao deve exigir unidade ativa quando a unidade nao mudou")
    void updateShouldAllowUnchangedInactiveUnit() {
        UnitConversion entity = sampleEntity();
        entity.getFromUnit().setActive(false);
        Unit from = entity.getFromUnit();
        Unit to = entity.getToUnit();
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        stubUnits(from, to);
        when(mapper.toResponse(entity)).thenReturn(responseOf(entity));

        service.update(requestFor(from, to), entity.getId());

        verify(repository).save(entity);
    }

    @Test
    @DisplayName("update deve rejeitar troca para unidade inativa")
    void updateShouldRejectChangeToInactiveUnit() {
        UnitConversion entity = sampleEntity();
        Unit from = entity.getFromUnit();
        Unit inactive = unit("MG", UnitType.MASS, false);
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        stubUnits(from, inactive);

        assertThatThrownBy(() -> service.update(requestFor(from, inactive), entity.getId()))
                .isInstanceOf(BusinessRuleException.class);
        verify(repository, never()).save(any());
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
