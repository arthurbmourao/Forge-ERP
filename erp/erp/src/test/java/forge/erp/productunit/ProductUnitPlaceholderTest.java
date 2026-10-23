package forge.erp.productunit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Placeholder de testes para a futura entidade ProductUnit.
 *
 * A entidade ProductUnit ainda nao existe no projeto (depende de Product, que tambem
 * ainda nao foi criado). Conforme a documentacao (item 11), ProductUnit tera:
 *   id, product (N:1 Product), unit (N:1 Unit), conversionFactor (BigDecimal),
 *   purchaseUnit (Boolean), issueUnit (Boolean), active (Boolean).
 *
 * ProductUnit representa unidades especificas usadas por um produto (ex.: 1 CAIXA = 100 UN),
 * portanto a conversao pertence ao produto e nao e universal.
 *
 * Quando ProductUnit/ProductUnitMapper/ProductUnitService forem criados, siga o mesmo
 * padrao usado em Category/Unit:
 *   - ProductUnitMapperTest  -> instanciar o mapper diretamente, validar toEntity/toResponse
 *   - ProductUnitServiceTest -> @ExtendWith(MockitoExtension.class) com @Mock repository/mapper
 *                               e @InjectMocks service; cobrir save/findById/findAll/
 *                               update/delete/turnOn/turnOff.
 *
 * Casos de teste sugeridos para a regra de negocio especifica de ProductUnit:
 *   - save deve exigir product e unit (obrigatorios);
 *   - conversionFactor obrigatorio e deve ser positivo (ex.: 1 CAIXA = 100 UN);
 *   - purchaseUnit indica se a unidade pode ser usada na compra;
 *   - issueUnit indica se a unidade pode ser usada na saida;
 *   - active controla o status (turnOn/turnOff).
 *
 * Este teste esta desabilitado para nao quebrar o build enquanto a entidade nao existe.
 * Remova o @Disabled e implemente os casos acima apos criar ProductUnit.
 */
@Disabled("Entidade ProductUnit ainda nao implementada - habilitar apos a criacao de ProductUnit")
class ProductUnitPlaceholderTest {

    @Test
    void productUnitTestsPendingImplementation() {
        // Implementar apos a criacao da entidade ProductUnit.
    }
}
