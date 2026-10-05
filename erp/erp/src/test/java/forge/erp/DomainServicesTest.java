package forge.erp;

import forge.erp.category.dto.CategoryRequest;
import forge.erp.category.dto.CategoryResponse;
import forge.erp.category.mapper.CategoryMapper;
import forge.erp.category.model.Category;
import forge.erp.category.repository.CategoryRepository;
import forge.erp.category.service.CategoryService;
import forge.erp.unit.dto.UnitDtoRequest;
import forge.erp.unit.dto.UnitDtoResponse;
import forge.erp.unit.enums.UnitType;
import forge.erp.unit.mapper.UnitMapper;
import forge.erp.unit.model.Unit;
import forge.erp.unit.repository.UnitRepository;
import forge.erp.unit.service.UnitService;
import forge.erp.unitconversion.dto.UnitConversionRequest;
import forge.erp.unitconversion.dto.UnitConversionResponse;
import forge.erp.unitconversion.mapper.UnitConversionMapper;
import forge.erp.unitconversion.model.UnitConversion;
import forge.erp.unitconversion.repository.UnitConversionRepository;
import forge.erp.unitconversion.service.UnitConversionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DomainServicesTest {

    @Mock UnitRepository unitRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock UnitConversionRepository conversionRepository;

    private final UnitMapper unitMapper = new UnitMapper();
    private final CategoryMapper categoryMapper = new CategoryMapper();
    private final UnitConversionMapper conversionMapper = new UnitConversionMapper();

    @Test
    void savesUnitAndReturnsMappedResponse() {
        Unit saved = new Unit("KG", "Kilogram", "kg", UnitType.MASS, 3, true);
        saved.setId(UUID.randomUUID());
        when(unitRepository.save(any(Unit.class))).thenReturn(saved);

        UnitDtoResponse response = new UnitService(unitMapper, unitRepository)
                .saveUnit(new UnitDtoRequest("Kilogram", "KG", "kg", UnitType.MASS, 3, true));

        assertEquals(saved.getId(), response.id());
        assertEquals("KG", response.code());
        assertEquals("Kilogram", response.name());
        verify(unitRepository).save(any(Unit.class));
    }

    @Test
    void unitActivationAndDeactivationChangeThePersistedEntity() {
        Unit unit = new Unit("KG", "Kilogram", "kg", UnitType.MASS, 3, false);
        UUID id = UUID.randomUUID();
        when(unitRepository.findById(id)).thenReturn(Optional.of(unit));
        UnitService service = new UnitService(unitMapper, unitRepository);

        service.turnOn(id);
        assertTrue(unit.isActive());
        service.turnOff(id);
        assertFalse(unit.isActive());
    }

    @Test
    void savesCategoryWithRequestCodeAndName() {
        Category saved = new Category("FASTENER", "Fixadores", "Peças de fixação");
        saved.setId(UUID.randomUUID());
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponse response = new CategoryService(categoryMapper, categoryRepository)
                .saveCategory(new CategoryRequest("FASTENER", "Fixadores", "Peças de fixação"));

        assertEquals(saved.getId(), response.id());
        assertEquals("FASTENER", response.code());
        assertEquals("Fixadores", response.name());
    }

    @Test
    void updatesCategoryFieldsFromRequest() {
        UUID id = UUID.randomUUID();
        Category category = new Category("OLD", "Antiga", "Descrição antiga");
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        CategoryResponse response = new CategoryService(categoryMapper, categoryRepository)
                .update(id, new CategoryRequest("NEW", "Nova", "Descrição nova"));

        assertEquals("NEW", category.getCode());
        assertEquals("Nova", category.getName());
        assertEquals("Descrição nova", category.getDescription());
        assertEquals("NEW", response.code());
    }

    @Test
    void savesUnitConversionAndReturnsTheSavedValues() {
        Unit from = new Unit("KG", "Kilogram", "kg", UnitType.MASS, 3, true);
        Unit to = new Unit("G", "Gram", "g", UnitType.MASS, 0, true);
        UnitConversion saved = new UnitConversion(from, to, new BigDecimal("1000"), true);
        saved.setId(UUID.randomUUID());
        when(conversionRepository.save(any(UnitConversion.class))).thenReturn(saved);

        UnitConversionResponse response = new UnitConversionService(conversionRepository, conversionMapper)
                .saveUnitConversion(new UnitConversionRequest(from, to, new BigDecimal("1000"), true));

        assertEquals(saved.getId(), response.id());
        assertSame(from, response.fromUnit());
        assertSame(to, response.toUnit());
        assertEquals(new BigDecimal("1000"), response.factor());
    }

    @Test
    void listsCategoriesThroughTheMapper() {
        Category category = new Category("FASTENER", "Fixadores", "Peças de fixação");
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<CategoryResponse> response = new CategoryService(categoryMapper, categoryRepository).findAll();

        assertEquals(1, response.size());
        assertEquals("FASTENER", response.get(0).code());
    }
}
