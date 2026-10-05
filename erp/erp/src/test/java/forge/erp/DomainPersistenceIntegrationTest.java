package forge.erp;

import forge.erp.category.model.Category;
import forge.erp.category.repository.CategoryRepository;
import forge.erp.category.dto.CategoryRequest;
import forge.erp.category.service.CategoryService;
import forge.erp.unit.enums.UnitType;
import forge.erp.unit.model.Unit;
import forge.erp.unit.repository.UnitRepository;
import forge.erp.unitconversion.model.UnitConversion;
import forge.erp.unitconversion.repository.UnitConversionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class DomainPersistenceIntegrationTest {

    @Autowired UnitRepository unitRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired UnitConversionRepository conversionRepository;
    @Autowired CategoryService categoryService;

    @Test
    void persistsAndReloadsUnit() {
        Unit unit = unitRepository.save(new Unit("TST", "Test unit", "t", UnitType.QUANTITY, 0, true));

        Unit reloaded = unitRepository.findById(unit.getId()).orElseThrow();

        assertEquals("TST", reloaded.getCode());
        assertEquals("Test unit", reloaded.getName());
        assertEquals(UnitType.QUANTITY, reloaded.getType());
        assertTrue(reloaded.isActive());
        assertNotNull(reloaded.getCreatedAt());
    }

    @Test
    void persistsAndReloadsCategory() {
        Category category = categoryRepository.save(new Category("TEST-CAT", "Test category", "Integration test"));

        Category reloaded = categoryRepository.findById(category.getId()).orElseThrow();

        assertEquals("TEST-CAT", reloaded.getCode());
        assertEquals("Test category", reloaded.getName());
        assertTrue(reloaded.getActive());
    }

    @Test
    void updatesCategoryThroughServiceAndPersistsTheNewValues() {
        Category category = categoryRepository.save(new Category("OLD-CAT", "Old name", "Old description"));

        categoryService.update(category.getId(), new CategoryRequest("NEW-CAT", "New name", "New description"));

        Category reloaded = categoryRepository.findById(category.getId()).orElseThrow();
        assertEquals("NEW-CAT", reloaded.getCode());
        assertEquals("New name", reloaded.getName());
        assertEquals("New description", reloaded.getDescription());
    }

    @Test
    void persistsConversionAndItsTwoUnitAssociations() {
        Unit from = unitRepository.save(new Unit("TSTKG", "Test kilogram", "tkg", UnitType.MASS, 3, true));
        Unit to = unitRepository.save(new Unit("TSTG", "Test gram", "tg", UnitType.MASS, 0, true));
        UnitConversion conversion = conversionRepository.save(
                new UnitConversion(from, to, new BigDecimal("1000.000000"), true));

        UnitConversion reloaded = conversionRepository.findById(conversion.getId()).orElseThrow();

        assertEquals(from.getId(), reloaded.getFromUnit().getId());
        assertEquals(to.getId(), reloaded.getToUnit().getId());
        assertEquals(new BigDecimal("1000.000000"), reloaded.getFactor());
        assertTrue(reloaded.isActive());
    }
}
