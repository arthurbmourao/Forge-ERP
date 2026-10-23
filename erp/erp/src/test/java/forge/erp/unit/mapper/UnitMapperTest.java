package forge.erp.unit.mapper;

import forge.erp.unit.dto.UnitDtoRequest;
import forge.erp.unit.dto.UnitDtoResponse;
import forge.erp.unit.enums.UnitType;
import forge.erp.unit.model.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UnitMapperTest {

    private UnitMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UnitMapper();
    }

    @Test
    @DisplayName("toEntity deve copiar os campos do request e nao definir code")
    void toEntityShouldMapRequestFields() {
        UnitDtoRequest request = new UnitDtoRequest("Quilograma", "kg", UnitType.MASS, 3, true);

        Unit entity = mapper.toEntity(request);

        // code e gerado no servidor, nao vem do request
        assertThat(entity.getCode()).isNull();
        assertThat(entity.getName()).isEqualTo("Quilograma");
        assertThat(entity.getSymbol()).isEqualTo("kg");
        assertThat(entity.getType()).isEqualTo(UnitType.MASS);
        assertThat(entity.getDecimalPlaces()).isEqualTo(3);
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    @DisplayName("toResponse deve mapear todos os campos da entidade")
    void toResponseShouldMapEntityFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Unit unit = new Unit("M", "Metro", "m", UnitType.LENGTH, 2, true);
        unit.setId(id);
        unit.setCreatedAt(now);
        unit.setUpdatedAt(now);

        UnitDtoResponse response = mapper.toResponse(unit);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.name()).isEqualTo("Metro");
        assertThat(response.code()).isEqualTo("M");
        assertThat(response.symbol()).isEqualTo("m");
        assertThat(response.type()).isEqualTo(UnitType.LENGTH);
        assertThat(response.decimalPlaces()).isEqualTo(2);
        assertThat(response.active()).isTrue();
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.updateAt()).isEqualTo(now);
    }
}
