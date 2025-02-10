package kr.co.pennyway.infra.client.coordinator;

import java.util.Objects;

public final class WebSocket {
    public record ChatServerUrl(String url) {
        public ChatServerUrl {
            Objects.requireNonNull(url, "url must not be null");
        }

        public static ChatServerUrl of(String url) {
            return new ChatServerUrl(url);
        }
    }
}
