package net.steinkopf.tuerauf.repository;

import net.steinkopf.tuerauf.data.AccessLog;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "accesslogs", path = "accesslogs")
// @Secured({"ROLE_ADMIN"})
public interface AccessLogRepository extends PagingAndSortingRepository<AccessLog, Long> {

}
