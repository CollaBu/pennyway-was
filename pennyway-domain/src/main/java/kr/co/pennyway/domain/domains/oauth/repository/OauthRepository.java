package kr.co.pennyway.domain.domains.oauth.repository;

import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OauthRepository extends JpaRepository<Oauth, Long> {
    Optional<Oauth> findByOauthIdAndProvider(String oauthId, Provider provider);

    boolean existsByUser_IdAndProvider(Long userId, Provider provider);
}
