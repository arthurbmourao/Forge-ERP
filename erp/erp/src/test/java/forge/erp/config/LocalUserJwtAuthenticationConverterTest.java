package forge.erp.config;

import forge.erp.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalUserJwtAuthenticationConverterTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private LocalUserJwtAuthenticationConverter converter;

    private Jwt jwt() {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("sub-123")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }

    @Test
    @DisplayName("deve usar as authorities do banco local, nao as do token")
    void shouldUseLocalAuthorities() {
        Jwt jwt = jwt();
        when(userService.syncAndResolveAuthorities(jwt)).thenReturn(Set.of("ROLE_VIEWER", "CATEGORY_READ"));

        AbstractAuthenticationToken auth = converter.convert(jwt);

        assertThat(auth.getName()).isEqualTo("sub-123");
        assertThat(auth.isAuthenticated()).isTrue();
        assertThat(auth.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_VIEWER", "CATEGORY_READ");
    }

    @Test
    @DisplayName("usuario inativo deve gerar autenticacao sem authorities")
    void inactiveUserShouldHaveNoAuthorities() {
        Jwt jwt = jwt();
        when(userService.syncAndResolveAuthorities(jwt)).thenReturn(Set.of());

        AbstractAuthenticationToken auth = converter.convert(jwt);

        assertThat(auth.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("deve tentar de novo uma vez quando outra requisicao criou o mesmo usuario ao mesmo tempo")
    void shouldRetryOnceOnConcurrentCreation() {
        Jwt jwt = jwt();
        when(userService.syncAndResolveAuthorities(jwt))
                .thenThrow(new DataIntegrityViolationException("uk_users_keycloak_id"))
                .thenReturn(Set.of("ROLE_VIEWER"));

        AbstractAuthenticationToken auth = converter.convert(jwt);

        assertThat(auth.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_VIEWER");
        verify(userService, times(2)).syncAndResolveAuthorities(jwt);
    }

    @Test
    @DisplayName("deve propagar o erro se a segunda tentativa tambem falhar")
    void shouldPropagateWhenRetryAlsoFails() {
        Jwt jwt = jwt();
        when(userService.syncAndResolveAuthorities(jwt))
                .thenThrow(new DataIntegrityViolationException("falha"));

        assertThatThrownBy(() -> converter.convert(jwt))
                .isInstanceOf(DataIntegrityViolationException.class);
        verify(userService, times(2)).syncAndResolveAuthorities(jwt);
    }
}
