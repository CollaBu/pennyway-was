package kr.co.pennyway.domain.domains.refresh.repository;

import kr.co.pennyway.domain.domains.refresh.domain.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String>, RefreshTokenCustomRepository {
}