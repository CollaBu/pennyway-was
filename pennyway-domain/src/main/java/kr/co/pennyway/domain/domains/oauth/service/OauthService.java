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

    @Transactional(readOnly = true)
    public Optional<Oauth> readOauthByOauthIdAndProvider(String oauthId, Provider provider) {
        return oauthRepository.findByOauthIdAndProviderAndDeletedAtIsNull(oauthId, provider);
    }

    @Transactional(readOnly = true)
    public Optional<Oauth> readOauthByUserIdAndProvider(Long userId, Provider provider) {
        return oauthRepository.findByUser_IdAndProvider(userId, provider);
    }

    @Transactional(readOnly = true)
    public Set<Oauth> readOauthsByUserId(Long userId) {
        return oauthRepository.findAllByUser_Id(userId);
    }

    @Transactional(readOnly = true)
    public boolean isExistOauthAccount(Long userId, Provider provider) {
        return oauthRepository.existsByUser_IdAndProvider(userId, provider);
    }

    @Transactional
    public void deleteOauth(Oauth oauth) {
        oauthRepository.delete(oauth);
    }

    @Transactional
    public void deleteOauthsByUserId(Long userId) {
        oauthRepository.deleteAllByUser_IdAndDeletedAtNullInQuery(userId);
    }
}
