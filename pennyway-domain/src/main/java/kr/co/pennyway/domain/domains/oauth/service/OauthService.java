package kr.co.pennyway.domain.domains.oauth.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
import kr.co.pennyway.domain.domains.oauth.repository.OauthRepository;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@DomainService
@RequiredArgsConstructor
public class OauthService {
    private final OauthRepository oauthRepository;

    @Transactional
    public Oauth createOauth(Oauth oauth) {
        return oauthRepository.save(oauth);
    }

    @Transactional(readOnly = true)
    public Optional<Oauth> readOauthByOauthIdAndProvider(String oauthId, Provider provider) {
        return oauthRepository.findByOauthIdAndProvider(oauthId, provider);
    }

    @Transactional(readOnly = true)
    public boolean isExistOauthAccount(Long userId, Provider provider) {
        return oauthRepository.existsByUser_IdAndProvider(userId, provider);
    }
}
