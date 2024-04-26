package kr.co.pennyway.api.config.fixture;

import kr.co.pennyway.api.common.security.authentication.CustomGrantedAuthority;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.type.ProfileVisibility;
import kr.co.pennyway.domain.domains.user.type.Role;

import java.util.List;

public enum UserFixture {
    GENERAL_USER(1L, "jayang", "dkssudgktpdy1", "Yang", "010-1111-1111", Role.USER, ProfileVisibility.PUBLIC, false),
    OAUTH_USER(2L, "only._.o", null, "Only", "0101-2222-2222", Role.USER, ProfileVisibility.PUBLIC, false),
    ;

    private final Long id;
    private final String username;
    private final String password;
    private final String name;
    private final String phone;
    private final Role role;
    private final ProfileVisibility profileVisibility;
    private final Boolean locked;

    UserFixture(Long id, String username, String password, String name, String phone, Role role, ProfileVisibility profileVisibility, Boolean locked) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.profileVisibility = profileVisibility;
        this.locked = locked;
    }

    public static SecurityUserDetails createSecurityUser(Long userId) {
        return SecurityUserDetails.builder()
                .userId(userId)
                .username(GENERAL_USER.username)
                .authorities(List.of(new CustomGrantedAuthority(GENERAL_USER.role.getType())))
                .accountNonLocked(false)
                .build();
    }

    public User toUser() {
        return User.builder()
                .username(username)
                .password(password)
                .name(name)
                .phone(phone)
                .role(role)
                .profileVisibility(profileVisibility)
                .locked(locked)
                .build();
    }
}