package kr.co.pennyway.domain.domains.oauth.repository;

import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

public interface OauthRepository extends JpaRepository<Oauth, Long> {
    Optional<Oauth> findByOauthIdAndProviderAndDeletedAtIsNull(String oauthId, Provider provider);

    Optional<Oauth> findByUser_IdAndProvider(Long userId, Provider provider);

    Set<Oauth> findAllByUser_Id(Long userId);

    boolean existsByUser_IdAndProvider(Long userId, Provider provider);

    boolean existsByOauthIdAndProviderAndDeletedAtIsNull(String oauthId, Provider provider);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Oauth o SET o.deletedAt = NOW() WHERE o.user.id = :userId AND o.deletedAt IS NULL")
    void deleteAllByUser_IdAndDeletedAtNullInQuery(Long userId);
}
