package kr.co.pennyway.api.apis.users.service;

import kr.co.pennyway.api.apis.users.dto.UserProfileDto;
import kr.co.pennyway.api.apis.users.mapper.UserProfileMapper;
import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
import kr.co.pennyway.domain.domains.oauth.service.OauthService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
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
    public UserProfileDto readMyAccount(Long userId) {
        User user = userService.readUser(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.NOT_FOUND));
        Set<Oauth> oauths = oauthService.readOauthsByUserId(userId).stream().filter(oauth -> !oauth.isDeleted()).collect(Collectors.toUnmodifiableSet());

        return UserProfileMapper.toUserProfileDto(user, oauths);
    }
}
