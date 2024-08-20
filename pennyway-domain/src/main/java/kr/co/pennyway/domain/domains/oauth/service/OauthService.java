package kr.co.pennyway.domain.domains.oauth.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
import kr.co.pennyway.domain.domains.oauth.repository.OauthRepository;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@DomainService
@RequiredArgsConstructor
public class OauthService {
    private final OauthRepository oauthRepository;

    @Transactional
    public Oauth createOauth(Oauth oauth) {
        return oauthRepository.save(oauth);
    }

    @Transactional
    public Optional<Oauth> readOauth(Long id) {
        return oauthRepository.findById(id);
    }

    /**
     * oauthId와 provider로 Oauth를 조회한다. 이 때, deletedAt이 null인 Oauth만 조회한다.
     */
    @Transactional(readOnly = true)
    public Optional<Oauth> readOauthByOauthIdAndProvider(String oauthId, Provider provider) {
        return oauthRepository.findByOauthIdAndProviderAndDeletedAtIsNull(oauthId, provider);
    }

    /**
     * userId와 provider로 Oauth를 조회한다. 이 때, deletedAt이 null인 Oauth만 조회한다.
     */
    @Transactional(readOnly = true)
    public Optional<Oauth> readOauthByUserIdAndProvider(Long userId, Provider provider) { // delete_at 옵션 없어서 불안
        return oauthRepository.findByUser_IdAndProviderAndDeletedAtIsNull(userId, provider);
    }

    @Transactional(readOnly = true)
    public Set<Oauth> readOauthsByUserId(Long userId) {
        return oauthRepository.findAllByUser_Id(userId);
    }

    /**
     * userId와 provider로 Oauth가 존재하는지 확인한다. 이 때, deletedAt이 null인 Oauth만 조회한다.
     */
    @Transactional(readOnly = true)
    public boolean isExistOauthByUserIdAndProvider(Long userId, Provider provider) { // delete_at 옵션 없어서 불안
        return oauthRepository.existsByUser_IdAndProviderAndDeletedAtIsNull(userId, provider);
    }

    /**
     * oauthId와 provider로 Oauth가 존재하는지 확인한다. 이 때, deletedAt이 null인 Oauth만 조회한다.
     */
    @Transactional(readOnly = true)
    public boolean isExistOauthByOauthIdAndProvider(String oauthId, Provider provider) {
        return oauthRepository.existsByOauthIdAndProviderAndDeletedAtIsNull(oauthId, provider);
    }

    @Transactional
    public void deleteOauth(Oauth oauth) {
        oauthRepository.delete(oauth);
    }

    @Transactional
    public void deleteOauthsByUserIdInQuery(Long userId) {
        oauthRepository.deleteAllByUser_IdAndDeletedAtNullInQuery(userId);
    }
}
