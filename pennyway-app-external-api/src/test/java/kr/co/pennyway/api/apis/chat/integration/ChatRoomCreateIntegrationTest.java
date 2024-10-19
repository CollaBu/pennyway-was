package kr.co.pennyway.api.apis.chat.integration;

import kr.co.pennyway.api.apis.chat.dto.ChatRoomReq;
import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenClaim;
import kr.co.pennyway.api.common.storage.AwsS3Adapter;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.ExternalApiIntegrationTest;
import kr.co.pennyway.api.config.fixture.ChatRoomFixture;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.service.UserService;
import kr.co.pennyway.infra.client.aws.s3.ActualIdProvider;
import kr.co.pennyway.infra.common.jwt.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.Map;

import static org.mockito.BDDMockito.given;


@Slf4j
@ExternalApiIntegrationTest
public class ChatRoomPendIntegrationTest extends ExternalApiDBTestConfig {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtProvider accessTokenProvider;

    @MockBean
    private AwsS3Adapter awsS3Adapter;

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("사용자는 채팅방 생성에 성공한다.")
    void success() {
        // given
        User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
        ChatRoom room = ChatRoomFixture.PRIVATE_CHAT_ROOM.toEntity();
        ChatRoomReq.Pend request = new ChatRoomReq.Pend(room.getTitle(), room.getDescription(), room.getPassword());
        ChatRoomReq.Create request2 = new ChatRoomReq.Create(room.getBackgroundImageUrl());
        given(awsS3Adapter.saveImage(room.getBackgroundImageUrl(), ActualIdProvider.createInstanceOfChatProfile())).willReturn("chatroom/1");

        // when
        ResponseEntity<SuccessResponse<Map<String, Long>>> response = postPending(user, request);
        ResponseEntity<SuccessResponse<Map<String, ChatRoomRes.Detail>>> response2 = postCreating(user, request2);
        ChatRoomRes.Detail detail = response2.getBody().getData().get("chatRoom");

        // then
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(), "201 Created 응답을 받아야 합니다.");
        Assertions.assertEquals(request.title(), detail.title(), "생성된 채팅방의 제목이 일치해야 합니다.");
        Assertions.assertEquals(request.description(), detail.description(), "생성된 채팅방의 설명이 일치해야 합니다.");
        Assertions.assertTrue(detail.isPrivate(), "생성된 채팅방은 비공개여야 합니다.");
    }

    private ResponseEntity<SuccessResponse<Map<String, Long>>> postPending(User user, ChatRoomReq.Pend request) {
        return restTemplate.exchange(
                "http://localhost:" + port + "/v2/chat-rooms/pend",
                HttpMethod.POST,
                createHttpEntity(user, request),
                new ParameterizedTypeReference<>() {
                }
        );
    }

    private ResponseEntity<SuccessResponse<Map<String, ChatRoomRes.Detail>>> postCreating(User user, ChatRoomReq.Create request) {
        return restTemplate.exchange(
                "http://localhost:" + port + "/v2/chat-rooms",
                HttpMethod.POST,
                createHttpEntity(user, request),
                new ParameterizedTypeReference<>() {
                }
        );
    }

    private HttpEntity<?> createHttpEntity(User user, ChatRoomReq.Pend request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessTokenProvider.generateToken(AccessTokenClaim.of(user.getId(), user.getRole().name())));
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(request, headers);
    }

    private HttpEntity<?> createHttpEntity(User user, ChatRoomReq.Create request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessTokenProvider.generateToken(AccessTokenClaim.of(user.getId(), user.getRole().name())));
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(request, headers);
    }
}
