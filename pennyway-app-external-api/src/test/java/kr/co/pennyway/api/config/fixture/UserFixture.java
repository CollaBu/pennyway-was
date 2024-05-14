package kr.co.pennyway.api.config.fixture;

import kr.co.pennyway.api.common.security.authentication.CustomGrantedAuthority;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.type.ProfileVisibility;
import kr.co.pennyway.domain.domains.user.type.Role;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.time.LocalDateTime;
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

    /**
     * 사용자의 가입일을 지정하여 DB에 저장하는 메서드
     *
     * @param joinedAt LocalDateTime : 가입일
     */
    public static User insertUserJoinedAt(User user, LocalDateTime joinedAt, NamedParameterJdbcTemplate jdbcTemplate) {
        String sql = String.format("""
                INSERT INTO `user` (id, username, name, password, password_updated_at, profile_image_url, phone, role, profile_visibility, locked, created_at, updated_at, deleted_at, account_book_notify, feed_notify, chat_notify)
                VALUES (:id, :username, :name, :password, %s, NULL, :phone, '1', '0', :locked, %s, NOW(), NULL, 1, 1, 1)
                """, joinedAt, joinedAt);
        SqlParameterSource param = new BeanPropertySqlParameterSource(user);
        jdbcTemplate.update(sql, param);

        return user;
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