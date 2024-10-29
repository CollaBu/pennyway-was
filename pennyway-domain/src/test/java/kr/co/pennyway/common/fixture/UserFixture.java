package kr.co.pennyway.common.fixture;

import kr.co.pennyway.domain.domains.user.domain.NotifySetting;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.type.ProfileVisibility;
import kr.co.pennyway.domain.domains.user.type.Role;
import lombok.Getter;

@Getter
public enum UserFixture {
    GENERAL_USER(1L, "jayang", "dkssudgktpdy1", "Yang", "010-1111-1111", Role.USER, ProfileVisibility.PUBLIC, NotifySetting.of(true, true, true), false),
    OAUTH_USER(2L, "only._.o", null, "Only", "010-2222-2222", Role.USER, ProfileVisibility.PUBLIC, NotifySetting.of(true, true, true), false),
    ;

    private final Long id;
    private final String username;
    private final String password;
    private final String name;
    private final String phone;
    private final Role role;
    private final ProfileVisibility profileVisibility;
    private final NotifySetting notifySetting;
    private final Boolean locked;

    UserFixture(Long id, String username, String password, String name, String phone, Role role, ProfileVisibility profileVisibility, NotifySetting notifySetting, Boolean locked) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.profileVisibility = profileVisibility;
        this.notifySetting = notifySetting;
        this.locked = locked;
    }

    public User toUser() {
        return User.builder()
                .username(username)
                .password(password)
                .name(name)
                .phone(phone)
                .role(role)
                .profileVisibility(profileVisibility)
                .notifySetting(notifySetting)
                .locked(locked)
                .build();
    }
}