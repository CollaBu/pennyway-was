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

    @Transactional(readOnly = true)
    public Optional<Oauth> getOauthByOauthIdAndProvider(String oauthId, Provider provider) {
        return oauthRepository.findByOauthIdAndProvider(oauthId, provider);
    }
}
