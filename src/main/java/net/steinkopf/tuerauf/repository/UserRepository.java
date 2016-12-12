package net.steinkopf.tuerauf.repository;

import net.steinkopf.tuerauf.data.User;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "users", path = "users")
// @Secured({"ROLE_ADMIN"})
public interface UserRepository extends PagingAndSortingRepository<User, Long> {

    List<User> findByActive(boolean active);

    List<User> findByActiveFalseAndNewUserTrue();

    List<User> findByInstallationId(String installationId);

    List<User> findBySerialId(int serialId);

    List<User> findByUsername(String username);
}
