package forge.erp.config;

import forge.erp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Converte um JWT valido do Keycloak em uma autenticacao do Spring Security cujas authorities
 * vem do banco local (User -> Role -> Permission), e nao do token.
 */
@Component
@RequiredArgsConstructor
public class LocalUserJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserService userService;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Set<String> names;
        try {
            names = userService.syncAndResolveAuthorities(jwt);
        } catch (DataIntegrityViolationException e) {
            // duas requisicoes simultaneas do mesmo usuario novo: a outra ja criou o registro
            names = userService.syncAndResolveAuthorities(jwt);
        }

        List<GrantedAuthority> authorities = names.stream()
                .<GrantedAuthority>map(SimpleGrantedAuthority::new)
                .toList();

        return new JwtAuthenticationToken(jwt, authorities);
    }
}
