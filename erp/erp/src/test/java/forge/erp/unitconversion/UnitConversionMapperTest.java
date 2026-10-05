package forge.erp.unitconversion;

import forge.erp.unit.enums.UnitType;
import forge.erp.unit.model.Unit;
import forge.erp.unitconversion.dto.UnitConversionRequest;
import forge.erp.unitconversion.dto.UnitConversionResponse;
import forge.erp.unitconversion.mapper.UnitConversionMapper;
import forge.erp.unitconversion.model.UnitConversion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnitConversionMapperTest {

    private final UnitConversionMapper mapper = new UnitConversionMapper();

    @Test
    void mapsRequestToEntityWithBothUnitsAndFactor() {
        Unit from = new Unit("KG", "Kilogram", "kg", UnitType.MASS, 3, true);
        Unit to = new Unit("G", "Gram", "g", UnitType.MASS, 0, true);
        BigDecimal factor = new BigDecimal("1000.000000");

        UnitConversion conversion = mapper.toEntity(new UnitConversionRequest(from, to, factor, true));

        assertSame(from, conversion.getFromUnit());
        assertSame(to, conversion.getToUnit());
        assertEquals(factor, conversion.getFactor());
        assertTrue(conversion.isActive());
    }

    @Test
    void mapsEntityToResponse() {
        Unit from = new Unit("KG", "Kilogram", "kg", UnitType.MASS, 3, true);
        Unit to = new Unit("G", "Gram", "g", UnitType.MASS, 0, true);
        UnitConversion conversion = new UnitConversion(from, to, new BigDecimal("1000"), true);
        UUID id = UUID.randomUUID();
        conversion.setId(id);

        UnitConversionResponse response = mapper.toResponse(conversion);

        assertEquals(id, response.id());
        assertSame(from, response.fromUnit());
        assertSame(to, response.toUnit());
        assertEquals(new BigDecimal("1000"), response.factor());
        assertTrue(response.active());
        assertNotNull(response.createdAt());
    }
}
