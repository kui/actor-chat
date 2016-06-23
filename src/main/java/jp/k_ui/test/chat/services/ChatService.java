package jp.k_ui.test.chat.services;

import java.time.Instant;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ChatService {
    public void onConnect(SessionContext context, Runnable callback) {
        log.info("{} enters {}", context.getUser(), context.getRoom());
        context.send.accept(new Message(Instant.now(), "Hello! this is " + context.getRoom().getName()));
        callback.run();
    }

    public void onReceive(SessionContext context, Message message) {
    }

    public void onClose(SessionContext context) {
        log.info("{} leaves {}", context.getUser(), context.getRoom());
    }

    @Value
    @RequiredArgsConstructor
    public static class Message {
        @NonNull
        private final Instant timestamp;
        @NonNull
        private final String message;
    }

    @Value
    @RequiredArgsConstructor
    public static class User {
        @NonNull
        private final String name;
    }

    @Value
    @RequiredArgsConstructor
    public static class Room {
        @NonNull
        private final String name;
    }

    @Value
    @RequiredArgsConstructor
    public static class SessionContext {
        @NonNull
        private WebSocketHttpExchange exchange;
        @NonNull
        private WebSocketChannel channel;
        @NonNull
        private User user;
        @NonNull
        private Room room;
        @NonNull
        private Consumer<Message> send;
    }
}
