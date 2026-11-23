package forge.erp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracoes de seguranca do Forge ERP (prefixo forge.security).
 *
 * @param bootstrapAdmin username (claim preferred_username do Keycloak) que se torna ADMIN no
 *                       acesso, enquanto ainda nao existir nenhum ADMIN no sistema. Vazio desabilita.
 *                       Variavel de ambiente: FORGE_BOOTSTRAP_ADMIN.
 */
@ConfigurationProperties(prefix = "forge.security")
public record ForgeSecurityProperties(String bootstrapAdmin) {
}
