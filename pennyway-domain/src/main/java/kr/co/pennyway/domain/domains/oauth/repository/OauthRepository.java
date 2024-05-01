package kr.co.pennyway.domain.domains.oauth.repository;

import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface OauthRepository extends JpaRepository<Oauth, Long> {
    Optional<Oauth> findByOauthIdAndProvider(String oauthId, Provider provider);

    Optional<Oauth> findByUser_IdAndProvider(Long userId, Provider provider);

    Set<Oauth> findAllByUser_Id(Long userId);

    boolean existsByUser_IdAndProvider(Long userId, Provider provider);
}
