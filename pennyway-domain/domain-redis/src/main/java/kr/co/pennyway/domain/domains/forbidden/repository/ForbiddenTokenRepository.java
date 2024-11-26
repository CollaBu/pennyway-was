package kr.co.pennyway.domain.domains.forbidden.repository;

import kr.co.pennyway.domain.domains.forbidden.domain.ForbiddenToken;
import org.springframework.data.repository.CrudRepository;

public interface ForbiddenTokenRepository extends CrudRepository<ForbiddenToken, String> {
}
