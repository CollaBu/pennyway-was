package kr.co.pennyway.domain.domains.user.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import kr.co.pennyway.domain.common.repository.QueryHandler;
import kr.co.pennyway.domain.config.ContainerMySqlTestConfig;
import kr.co.pennyway.domain.config.JpaConfig;
import kr.co.pennyway.domain.config.TestJpaConfig;
import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
import kr.co.pennyway.domain.domains.oauth.domain.QOauth;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import kr.co.pennyway.domain.domains.user.domain.NotifySetting;
import kr.co.pennyway.domain.domains.user.domain.QUser;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.type.ProfileVisibility;
import kr.co.pennyway.domain.domains.user.type.Role;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.time.LocalDateTime.now;
import static org.springframework.test.util.AssertionErrors.*;

@Slf4j
@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create"})
@ContextConfiguration(classes = {JpaConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
public class UserExtendedRepositoryTest extends ContainerMySqlTestConfig {
    private static final String USER_TABLE = "user";
    private static final String OAUTH_TABLE = "oauth";

    private final QUser qUser = QUser.user;
    private final QOauth qOauth = QOauth.oauth;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        List<User> users = getRandomUsers();
        bulkInsertUser(users);

        users = userRepository.findAll();

        List<Oauth> oauths = getRandomOauths(users);
        bulkInsertOauth(oauths);
    }

    @Test
    @DisplayName("""
            Entity findList 테스트: 이름이 양재서고, 일반 회원가입 이력이 존재하면서, lock이 걸려있지 않은 사용자 정보를 조회한다.
             이때, 결과는 id 내림차순으로 정렬한다.
            """)
    @Transactional
    public void findList() {
        // given
        Predicate predicate = qUser.name.eq("양재서")
                .and(qUser.password.isNotNull())
                .and(qUser.locked.isFalse());

        QueryHandler queryHandler = null; // queryHandler는 사용하지 않으므로 null로 설정

        Sort sort = Sort.by(Sort.Order.desc("id"));

        // when
        List<User> users = userRepository.findList(predicate, queryHandler, sort);

        // then
        Long maxValue = 100000L;
        for (User user : users) {
            log.info("user: {}", user);

            assertTrue("id는 내림차순 정렬되어야 한다.", user.getId() <= maxValue);
            assertTrue("일반 회원가입 이력이 존재해야 한다.", user.isGeneralSignedUpUser());
            assertFalse("lock이 걸려있지 않아야 한다.", user.isLocked());

            maxValue = user.getId();
        }
    }

    @Test
    @DisplayName("""
            Entity findPage 테스트: 이름이 양재서고, Kakao로 가입한 Oauth 정보를 조회한다.
            단, 결과는 처음 5개만 조회하며, id 내림차순으로 정렬한다.
            """)
    @Transactional
    public void findPage() {
        // given
        Predicate predicate = qUser.name.eq("양재서")
                .and(qOauth.provider.eq(Provider.KAKAO));

        QueryHandler queryHandler = query -> query.leftJoin(qOauth).on(qUser.id.eq(qOauth.user.id));
        Sort sort = Sort.by(Sort.Order.desc("user.id"));

        int pageNumber = 0, pageSize = 5;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        // when
        Page<User> users = userRepository.findPage(predicate, queryHandler, pageable);

        // then
        assertEquals("users의 크기는 5여야 한다.", 5, users.getSize());
        Long maxValue = 100000L;
        for (User user : users.getContent()) {
            log.debug("user: {}", user);
            assertTrue("id는 내림차순 정렬되어야 한다.", user.getId() <= maxValue);
            assertEquals("이름이 양재서여야 한다.", "양재서", user.getName());
            maxValue = user.getId();
        }
    }

    @Test
    @DisplayName("""
            Dto selectList 테스트: 사용자 이름이 양재서인 사용자의 username, name, phone 그리고 연동된 Oauth 정보를 조회한다.
            LinkedHashMap을 사용하여 Dto 생성자 파라미터 순서에 맞게 삽입하면, Dto의 불변성을 유지할 수 있다.
            """)
    @Transactional
    public void selectListUseLinkedHashMap() {
        // given
        Predicate predicate = qUser.name.eq("양재서");

        QueryHandler queryHandler = query -> query.leftJoin(qOauth).on(qUser.id.eq(qOauth.user.id));
        Sort sort = null;

        Map<String, Expression<?>> bindings = new LinkedHashMap<>();

        bindings.put("userId", qUser.id);
        bindings.put("username", qUser.username);
        bindings.put("name", qUser.name);
        bindings.put("phone", qUser.phone);
        bindings.put("oauthId", qOauth.id);
        bindings.put("provider", qOauth.provider);

        // when
        List<UserAndOauthInfo> userAndOauthInfos = userRepository.selectList(predicate, UserAndOauthInfo.class, bindings, queryHandler, sort);

        // then
        userAndOauthInfos.forEach(userAndOauthInfo -> {
            log.debug("userAndOauthInfo: {}", userAndOauthInfo);
            assertEquals("이름이 양재서인 사용자만 조회되어야 한다.", "양재서", userAndOauthInfo.name());
            assertEquals("provider는 KAKAO여야 한다.", Provider.KAKAO, userAndOauthInfo.provider());
        });
    }

    @Test
    @DisplayName("""
            Dto selectList 테스트: 사용자 이름이 양재서인 사용자의 username, name, phone 그리고 연동된 Oauth 정보를 조회한다.
            HashMap을 사용하더라도 Dto의 setter를 명시하고 final 키워드를 제거하면 결과를 조회할 수 있다.
            """)
    @Transactional
    public void selectListUseHashMap() {
        // given
        Predicate predicate = qUser.name.eq("양재서");

        QueryHandler queryHandler = query -> query.leftJoin(qOauth).on(qUser.id.eq(qOauth.user.id));
        Sort sort = null;

        Map<String, Expression<?>> bindings = new HashMap<>();

        bindings.put("userId", qUser.id);
        bindings.put("username", qUser.username);
        bindings.put("name", qUser.name);
        bindings.put("phone", qUser.phone);
        bindings.put("oauthId", qOauth.id);
        bindings.put("provider", qOauth.provider);

        // when
        List<UserAndOauthInfoNotImmutable> userAndOauthInfos = userRepository.selectList(predicate, UserAndOauthInfoNotImmutable.class, bindings, queryHandler, sort);

        // then
        userAndOauthInfos.forEach(userAndOauthInfo -> {
            log.debug("userAndOauthInfo: {}", userAndOauthInfo);
            assertEquals("이름이 양재서인 사용자만 조회되어야 한다.", "양재서", userAndOauthInfo.getName());
            assertEquals("provider는 KAKAO여야 한다.", Provider.KAKAO, userAndOauthInfo.getProvider());
        });
    }

    private List<User> getRandomUsers() {
        List<User> users = new ArrayList<>(100);
        List<String> name = List.of("양재서", "이진우", "안성윤", "최희진", "아우신얀", "강병준", "이의찬", "이수민", "이주원");

        for (int i = 0; i < 100; ++i) {
            User user = User.builder()
                    .username("jayang" + i)
                    .name(name.get(i % name.size()))
                    .password((i % 2 == 0) ? null : "password" + i)
                    .passwordUpdatedAt((i % 2 == 0) ? null : now())
                    .profileVisibility(ProfileVisibility.PUBLIC)
                    .phone("010-1111-1" + String.format("%03d", i))
                    .role(Role.USER)
                    .locked((i % 10 == 0))
                    .notifySetting(NotifySetting.of(true, true, true))
                    .build();

            users.add(user);
        }

        return users;
    }

    private List<Oauth> getRandomOauths(Collection<User> users) {
        List<Oauth> oauths = new ArrayList<>(users.size());

        for (User user : users) {
            Oauth oauth = Oauth.of(Provider.KAKAO, "providerId" + user.getId(), user);
            oauths.add(oauth);
        }

        return oauths;
    }

    private void bulkInsertUser(Collection<User> users) {
        String sql = String.format("""
                INSERT INTO `%s` (username, name, password, password_updated_at, profile_image_url, phone, role, profile_visibility, locked, created_at, updated_at, account_book_notify, feed_notify, chat_notify, deleted_at)
                VALUES (:username, :name, :password, :passwordUpdatedAt, :profileImageUrl, :phone, '1', '0', :locked, now(), now(), 1, 1, 1, :deletedAt)
                """, USER_TABLE);
        SqlParameterSource[] params = users.stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(SqlParameterSource[]::new);
        jdbcTemplate.batchUpdate(sql, params);
    }

    private void bulkInsertOauth(Collection<Oauth> oauths) {
        String sql = String.format("""
                INSERT INTO `%s` (provider, oauth_id, user_id, created_at, deleted_at)
                VALUES (1, :oauthId, :user.id, now(), NULL)
                """, OAUTH_TABLE);
        SqlParameterSource[] params = oauths.stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(SqlParameterSource[]::new);
        jdbcTemplate.batchUpdate(sql, params);
    }

    public record UserAndOauthInfo(Long userId, String username, String name, String phone, Long oauthId,
                                   Provider provider) {
        @Override
        public String toString() {
            return "UserAndOauthInfo{" +
                    "userId=" + userId +
                    ", username='" + username + '\'' +
                    ", name='" + name + '\'' +
                    ", phone='" + phone + '\'' +
                    ", oauthId=" + oauthId +
                    ", provider=" + provider +
                    '}';
        }
    }

    @Setter
    @Getter
    public static class UserAndOauthInfoNotImmutable {
        private Long userId;
        private String username;
        private String name;
        private String phone;
        private Long oauthId;
        private Provider provider;

        public UserAndOauthInfoNotImmutable() {
        }

        @Override
        public String toString() {
            return "UserAndOauthInfoNotImmutable{" +
                    "userId=" + userId +
                    ", username='" + username + '\'' +
                    ", name='" + name + '\'' +
                    ", phone='" + phone + '\'' +
                    ", oauthId=" + oauthId +
                    ", provider=" + provider +
                    '}';
        }
    }
}
