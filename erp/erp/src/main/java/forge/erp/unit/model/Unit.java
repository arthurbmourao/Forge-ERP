package forge.erp.unit.model;

import forge.erp.unit.enums.UnitType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "units")
public class Unit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    private String code;

    private String name;

    private String symbol;

    @Enumerated(EnumType.STRING)
    private UnitType type;

    private Integer decimalPlaces;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
