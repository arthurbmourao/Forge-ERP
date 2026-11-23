package forge.erp.user.repository;

import forge.erp.user.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // carrega papeis e permissoes numa unica consulta, pois sao lidos a cada requisicao autenticada
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<User> findByKeycloakId(String keycloakId);

    // usado pela gestao de usuarios: o mapper le o papel fora do contexto de persistencia
    @Override
    @EntityGraph(attributePaths = "roles")
    Optional<User> findById(UUID id);

    @EntityGraph(attributePaths = "roles")
    List<User> findAllByOrderByUsernameAsc();

    boolean existsByRolesName(String roleName);
}
