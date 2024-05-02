package kr.co.pennyway.api.apis.users.mapper;

import kr.co.pennyway.api.apis.users.dto.OauthAccountDto;
import kr.co.pennyway.api.apis.users.dto.UserProfileDto;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
import kr.co.pennyway.domain.domains.user.domain.User;

import java.util.Set;

@Mapper
public class UserProfileMapper {
    public static UserProfileDto toUserProfileDto(User user, Set<Oauth> oauths) {
        boolean kakao, google, apple;
        kakao = google = apple = false;

        for (Oauth oauth : oauths) {
            switch (oauth.getProvider()) {
                case KAKAO -> kakao = true;
                case GOOGLE -> google = true;
                case APPLE -> apple = true;
            }
        }

        return UserProfileDto.from(user, OauthAccountDto.of(kakao, google, apple));
    }
}
