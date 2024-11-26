package kr.co.pennyway.api.apis.users.service;

import kr.co.pennyway.domain.context.account.service.OauthService;
import kr.co.pennyway.domain.context.account.service.UserService;
import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileSearchService {
    private final UserService userService;
    private final OauthService oauthService;

    @Transactional(readOnly = true)
    public User readMyAccount(Long userId) {
        return userService.readUser(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Set<Oauth> readMyOauths(Long userId) {
        return oauthService.readOauthsByUserId(userId).stream().filter(oauth -> !oauth.isDeleted()).collect(Collectors.toUnmodifiableSet());
    }
}
