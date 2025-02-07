package kr.co.pennyway.api.apis.chat.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.api.apis.chat.dto.ChatMemberReq;
import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.api.common.response.ErrorResponse;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenClaim;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.ExternalApiIntegrationTest;
import kr.co.pennyway.api.config.fixture.ChatMemberFixture;
import kr.co.pennyway.api.config.fixture.ChatRoomFixture;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.exception.ChatRoomErrorCode;
import kr.co.pennyway.domain.domains.chatroom.repository.ChatRoomRepository;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorCode;
import kr.co.pennyway.domain.domains.member.repository.ChatMemberRepository;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.repository.UserRepository;
import kr.co.pennyway.infra.client.guid.IdGenerator;
import kr.co.pennyway.infra.common.event.ChatRoomJoinEvent;
import kr.co.pennyway.infra.common.event.ChatRoomJoinEventHandler;
import kr.co.pennyway.infra.common.jwt.JwtProvider;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@Slf4j
@ExternalApiIntegrationTest
@RecordApplicationEvents
public class ChatMemberJoinIntegrationTest extends ExternalApiDBTestConfig {
    private static final String BASE_URL = "/v2/chat-rooms/{chatRoomId}/chat-members";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMemberRepository chatMemberRepository;

    @Autowired
    private JwtProvider accessTokenProvider;

    @Autowired
    private IdGenerator<Long> idGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationEvents events;

    @MockBean
    private ChatRoomJoinEventHandler chatRoomJoinEventHandler;

    @LocalServerPort
    private int port;

    private String url;

    @BeforeEach
    void setUp() {
        url = "http://localhost:" + port + BASE_URL;
    }

    @Test
    @DisplayName("Happy Path: 공개 채팅방 가입 성공")
    void successJoinPublicRoom() {
        // given
        User admin = userRepository.save(UserFixture.GENERAL_USER.toUser());
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity(idGenerator.generate()));
        chatMemberRepository.save(ChatMemberFixture.ADMIN.toEntity(admin, chatRoom));

        User user = userRepository.save(UserFixture.GENERAL_USER.toUser());

        ArgumentCaptor<ChatRoomJoinEvent> eventCaptor = ArgumentCaptor.forClass(ChatRoomJoinEvent.class);

        // when
        ResponseEntity<?> response = postJoining(user, chatRoom.getId(), new ChatMemberReq.Join(null));

        // then
        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertTrue(chatMemberRepository.existsByChatRoomIdAndUserId(chatRoom.getId(), user.getId())),
                () -> verify(chatRoomJoinEventHandler).handle(eventCaptor.capture()),
                () -> {
                    ChatRoomJoinEvent capturedEvent = eventCaptor.getValue();
                    assertEquals(chatRoom.getId(), capturedEvent.chatRoomId());
                    assertEquals(user.getName(), capturedEvent.userName());
                }
        );
    }

    @Test
    @DisplayName("동시에 350명의 사용자가 가입을 시도하면 정원 초과로 인해, 299명만 가입에 성공한다")
    void concurrentJoinRequests() throws InterruptedException {
        // given
        User admin = userRepository.save(UserFixture.GENERAL_USER.toUser());
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity(idGenerator.generate()));
        chatMemberRepository.save(ChatMemberFixture.ADMIN.toEntity(admin, chatRoom));

        List<User> users = IntStream.range(0, 350)
                .mapToObj(i -> userRepository.save(UserFixture.GENERAL_USER.toUser()))
                .toList();

        // when
        CountDownLatch latch = new CountDownLatch(users.size());
        List<CompletableFuture<JoinResult>> futures = users.stream()
                .map(user -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return JoinResult.from(
                                postJoining(user, chatRoom.getId(), new ChatMemberReq.Join(null))
                        );
                    } finally {
                        latch.countDown();
                    }
                }))
                .toList();

        latch.await();

        List<JoinResult> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        // then
        assertAll(
                () -> assertEquals(299, results.stream().filter(JoinResult::isSuccess).count()),
                () -> assertEquals(51, results.stream().filter(JoinResult::isFullRoomError).count()),
                () -> assertEquals(300, chatMemberRepository.countByChatRoomIdAndActive(chatRoom.getId()))
        );
    }

    @Test
    @Disabled
    @DisplayName("트랜잭션 롤백: 이벤트 발행 실패 시 가입도 롤백된다")
    void rollbackWhenEventPublishFails() {
        // given
        User admin = userRepository.save(UserFixture.GENERAL_USER.toUser());
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity(idGenerator.generate()));
        chatMemberRepository.save(ChatMemberFixture.ADMIN.toEntity(admin, chatRoom));

        User user = userRepository.save(UserFixture.GENERAL_USER.toUser());

        doThrow(new RuntimeException("Event publish failed")).when(chatRoomJoinEventHandler).handle(any(ChatRoomJoinEvent.class));

        // when
        ResponseEntity<?> response = postJoining(user, chatRoom.getId(), new ChatMemberReq.Join(null));

        // then
        assertAll(
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode()),
                () -> assertFalse(chatMemberRepository.existsByChatRoomIdAndUserId(chatRoom.getId(), user.getId()))
        );
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 가입할 수 없다")
    void failWhenUserNotAuthenticated() {
        // given
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity(idGenerator.generate()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // when
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(new ChatMemberReq.Join(null), headers),
                new ParameterizedTypeReference<>() {
                },
                chatRoom.getId()
        );

        // then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("비공개 채팅방 가입 시 올바른 비밀번호로 가입할 수 있다")
    void successJoinPrivateRoomWithValidPassword() {
        // given
        User admin = userRepository.save(UserFixture.GENERAL_USER.toUser());
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity(idGenerator.generate()));
        chatMemberRepository.save(ChatMemberFixture.ADMIN.toEntity(admin, chatRoom));

        User user = userRepository.save(UserFixture.GENERAL_USER.toUser());
        Integer expectedPassword = ChatRoomFixture.PRIVATE_CHAT_ROOM.toEntity(idGenerator.generate()).getPassword();

        // when
        ResponseEntity<?> response = postJoining(user, chatRoom.getId(), new ChatMemberReq.Join(expectedPassword.toString()));

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("같은 사용자가 하나의 채팅방에 동시에 100개의 요청을 보내면, 100개의 가입 요청 중 1개만 성공한다")
    void concurrentJoinRequestsFromSameUser() throws InterruptedException {
        // given
        User admin = userRepository.save(UserFixture.GENERAL_USER.toUser());
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity(idGenerator.generate()));
        chatMemberRepository.save(ChatMemberFixture.ADMIN.toEntity(admin, chatRoom));

        User user = userRepository.save(UserFixture.GENERAL_USER.toUser());

        // when
        CountDownLatch latch = new CountDownLatch(100);
        List<CompletableFuture<JoinResult>> futures = IntStream.range(0, 100)
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return JoinResult.from(
                                postJoining(user, chatRoom.getId(), new ChatMemberReq.Join(null))
                        );
                    } finally {
                        latch.countDown();
                    }
                }))
                .toList();

        latch.await();

        List<JoinResult> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        // then
        assertAll(
                () -> assertEquals(1, results.stream().filter(JoinResult::isSuccess).count()),
                () -> assertEquals(99, results.stream().filter(JoinResult::isAlreadyJoinedError).count()),
                () -> assertEquals(2, chatMemberRepository.countByChatRoomIdAndActive(chatRoom.getId()))
        );
    }

    private ResponseEntity<?> postJoining(User user, Long chatRoomId, ChatMemberReq.Join request) {
        ResponseEntity<Object> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                createHttpEntity(user, request),
                Object.class,
                chatRoomId
        );

        Object body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("예상치 못한 반환 타입입니다. : " + response);
        }

        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity
                    .status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(objectMapper.convertValue(body, new TypeReference<SuccessResponse<Map<String, ChatRoomRes.Detail>>>() {
                    }));
        } else {
            return ResponseEntity
                    .status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(objectMapper.convertValue(body, new TypeReference<ErrorResponse>() {
                    }));
        }
    }

    private HttpEntity<?> createHttpEntity(User user, ChatMemberReq.Join request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessTokenProvider.generateToken(AccessTokenClaim.of(user.getId(), user.getRole().name())));
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(request, headers);
    }

    @Getter
    private static class JoinResult {
        private final HttpStatusCode status;
        private final Object body;
        private final boolean isSuccess;

        private JoinResult(ResponseEntity<?> response) {
            this.status = response.getStatusCode();
            this.body = response.getBody();
            this.isSuccess = status == HttpStatus.OK;
        }

        public static JoinResult from(ResponseEntity<?> response) {
            return new JoinResult(response);
        }

        public boolean isFullRoomError() {
            if (!isSuccess && body instanceof ErrorResponse errorResponse) {
                return errorResponse.getCode().equals(ChatRoomErrorCode.FULL_CHAT_ROOM.causedBy().getCode());
            }
            return false;
        }

        public boolean isAlreadyJoinedError() {
            if (!isSuccess && body instanceof ErrorResponse errorResponse) {
                return errorResponse.getCode().equals(ChatMemberErrorCode.ALREADY_JOINED.causedBy().getCode());
            }
            return false;
        }
    }
}
