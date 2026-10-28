package forge.erp.productunit.model;

import forge.erp.product.model.Product;
import forge.erp.unit.model.Unit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "product_units")
public class ProductUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    // quantas unidades de estoque do produto equivalem a 1 desta unidade (ex.: 1 CAIXA = 100 UN)
    @Column(precision = 19, scale = 6)
    private BigDecimal conversionFactor;

    private boolean purchaseUnit;

    private boolean issueUnit;

    private boolean active;

    public ProductUnit(
            Product product,
            Unit unit,
            BigDecimal conversionFactor,
            boolean purchaseUnit,
            boolean issueUnit,
            boolean active
    ) {
        this.product = product;
        this.unit = unit;
        this.conversionFactor = conversionFactor;
        this.purchaseUnit = purchaseUnit;
        this.issueUnit = issueUnit;
        this.active = active;
    }
}
