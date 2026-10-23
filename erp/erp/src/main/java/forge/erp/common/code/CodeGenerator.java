package forge.erp.common.code;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.function.Predicate;

@Component
public class CodeGenerator {

    private static final int MIN = 1;
    private static final int MAX = 9999;
    private static final int MAX_ATTEMPTS = 100;

    private final SecureRandom random = new SecureRandom();

    /**
     * Gera um codigo unico com o prefixo informado.
     *
     * @param prefix   prefixo do tipo da entidade (ex.: "CAT", "UNI")
     * @param existsFn predicado que retorna true se o codigo ja existe
     * @return codigo unico no formato PREFIXO-XXXX
     * @throws IllegalStateException se nao for possivel gerar um codigo unico
     */
    public String generate(String prefix, Predicate<String> existsFn) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            int number = MIN + random.nextInt(MAX - MIN + 1);
            String code = "%s-%04d".formatted(prefix, number);
            if (!existsFn.test(code)) {
                return code;
            }
        }
        throw new IllegalStateException(
                "Nao foi possivel gerar um codigo unico para o prefixo '%s' apos %d tentativas"
                        .formatted(prefix, MAX_ATTEMPTS));
    }
}
