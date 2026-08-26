package forge.erp.unitconversion.model;

import forge.erp.unit.model.Unit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "unit_conversions")
public class UnitConversion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private Unit fromUnit;

    private Unit toUnit;

    private Integer factor;

    private boolean active;

    private LocalDateTime createdAt;
}
