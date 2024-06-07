package kr.co.pennyway.domain.domains.oauth.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.pennyway.domain.config.ContainerMySqlTestConfig;
import kr.co.pennyway.domain.config.JpaConfig;
import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import kr.co.pennyway.domain.domains.user.domain.NotifySetting;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.repository.UserRepository;
import kr.co.pennyway.domain.domains.user.type.ProfileVisibility;
import kr.co.pennyway.domain.domains.user.type.Role;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Slf4j
@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create"})
@ContextConfiguration(classes = JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class OauthRepositoryTest extends ContainerMySqlTestConfig {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OauthRepository oauthRepository;

    @MockBean
    private JPAQueryFactory jpaQueryFactory;

    private User user;

    @Test
    @DisplayName("soft delete된 다른 user_id를 가지면서, 같은 oauth_id, provider를 갖는 정보가 존재해도, 하나의 결과만을 반환한다.")
    @Transactional
    public void test() {
        // given
        User user = createUser();
        Oauth oauth = Oauth.of(Provider.KAKAO, "oauth_id", user);

        User newUser = createUser();
        Oauth newOauth = Oauth.of(Provider.KAKAO, "oauth_id", newUser);

        // when (소셜 회원가입 ⇾ 회원 탈퇴 ⇾ 동일 정보 소셜 회원가입 ⇾ 조회 성공)
        userRepository.save(user);
        oauthRepository.save(oauth);
        log.debug("user: {}, oauth: {}", user, oauth);

        userRepository.delete(user);
        oauthRepository.delete(oauth);

        userRepository.save(newUser);
        oauthRepository.save(newOauth);
        log.debug("newUser: {}, newOauth: {}", newUser, newOauth);

        // then
        assertDoesNotThrow(() -> oauthRepository.findByOauthIdAndProviderAndDeletedAtIsNull(newOauth.getOauthId(), newOauth.getProvider()));
    }

    private User createUser() {
        return User.builder()
                .username("test")
                .name("pannyway")
                .password("test")
                .phone("010-1234-5678")
                .role(Role.USER)
                .profileVisibility(ProfileVisibility.PUBLIC)
                .notifySetting(NotifySetting.of(true, true, true))
                .build();
    }
}
