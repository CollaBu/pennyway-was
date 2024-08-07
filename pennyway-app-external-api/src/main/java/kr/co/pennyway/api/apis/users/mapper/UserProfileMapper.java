package kr.co.pennyway.api.apis.users.mapper;

import kr.co.pennyway.api.apis.users.dto.OauthAccountDto;
import kr.co.pennyway.api.apis.users.dto.UserProfileDto;
import kr.co.pennyway.api.apis.users.dto.UserProfileUpdateDto;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
import kr.co.pennyway.domain.domains.user.domain.NotifySetting;
import kr.co.pennyway.domain.domains.user.domain.User;

import java.util.Set;

@Mapper
public class UserProfileMapper {
    public static UserProfileDto toUserProfileDto(User user, Set<Oauth> oauths, String objectPrefix) {
        boolean kakao, google, apple;
        kakao = google = apple = false;

        for (Oauth oauth : oauths) {
            switch (oauth.getProvider()) {
                case KAKAO -> kakao = true;
                case GOOGLE -> google = true;
                case APPLE -> apple = true;
            }
        }

        String profileImageUrl = (user.getProfileImageUrl() == null) ? "" : objectPrefix + user.getProfileImageUrl();

        return UserProfileDto.from(user, profileImageUrl, OauthAccountDto.of(kakao, google, apple));
    }

    public static UserProfileUpdateDto.NotifySettingUpdateRes toNotifySettingUpdateRes(NotifySetting.NotifyType type, Boolean flag) {
        return switch (type) {
            case ACCOUNT_BOOK -> new UserProfileUpdateDto.NotifySettingUpdateRes(flag, null, null);
            case FEED -> new UserProfileUpdateDto.NotifySettingUpdateRes(null, flag, null);
            case CHAT -> new UserProfileUpdateDto.NotifySettingUpdateRes(null, null, flag);
        };
    }
}
