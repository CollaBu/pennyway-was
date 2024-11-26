package kr.co.pennyway.domain.context.account.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
import kr.co.pennyway.domain.domains.oauth.service.OauthRdbService;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class OauthService {
    private final OauthRdbService oauthRdbService;

    @Transactional
    public Oauth createOauth(Oauth oauth) {
        return oauthRdbService.createOauth(oauth);
    }

    @Transactional(readOnly = true)
    public Optional<Oauth> readOauth(Long id) {
        return oauthRdbService.readOauth(id);
    }

    // @Todo: Provider 파라미터를 제거하고, 선택 로직을 내부에서 처리
    @Transactional(readOnly = true)
    public Optional<Oauth> readOauthByOauthIdAndProvider(String oauthId, Provider provider) {
        return oauthRdbService.readOauthByOauthIdAndProvider(oauthId, provider);
    }

    @Transactional(readOnly = true)
    public Set<Oauth> readOauthsByUserId(Long userId) {
        return oauthRdbService.readOauthsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public boolean isExistOauthByUserIdAndProvider(Long userId, Provider provider) {
        return oauthRdbService.isExistOauthByUserIdAndProvider(userId, provider);
    }

    @Transactional(readOnly = true)
    public boolean isExistOauthByOauthIdAndProvider(String oauthId, Provider provider) {
        return oauthRdbService.isExistOauthByOauthIdAndProvider(oauthId, provider);
    }

    @Transactional
    public void deleteOauth(Oauth oauth) {
        oauthRdbService.deleteOauth(oauth);
    }

    @Transactional
    public void deleteOauth(Long oauthId) {
        oauthRdbService.deleteOauthsByUserIdInQuery(oauthId);
    }
}
