package net.steinkopf.tuerauf.repository;

import net.steinkopf.tuerauf.data.User;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(collectionResourceRel = "users", path = "users")
// @Secured({"ROLE_ADMIN"})
public interface UserRepository extends PagingAndSortingRepository<User, Long> {

    List<User> findByActive(boolean active);

    List<User> findByActiveFalseAndNewUserTrue();

    Optional<User> findByInstallationId(String installationId);

    Optional<User> findBySerialId(int serialId);

    Optional<User> findByUsername(String username);
}
