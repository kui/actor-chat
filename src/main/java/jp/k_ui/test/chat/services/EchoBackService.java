package jp.k_ui.test.chat.services;

import java.time.Instant;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import akka.dispatch.Futures;
import io.undertow.websockets.core.WebSocketCallback;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Autowired }))
public class EchoBackService {
    private final ObjectMapper objectMapper;

    public void onConnect(WebSocketHttpExchange exchange,
                          WebSocketChannel channel,
                          Consumer<Throwable> callback) {
        log.info("connect");

        send(new Message("Hello!", Instant.now()), channel, new WebSocketCallback<Void>() {
            @Override
            public void complete(WebSocketChannel channel, Void context) {
                callback.accept(null);
            }

            @Override
            public void onError(WebSocketChannel channel, Void context, Throwable throwable) {
                callback.accept(throwable);
            }
        });
    }

    public void onReceive(WebSocketHttpExchange exchange,
                          WebSocketChannel channel,
                          Message message) {
        send(message, channel, new WebSocketCallback<Void>() {
            @Override
            public void complete(WebSocketChannel channel, Void context) {
                log.info("echo-back: {}", message);
            }

            @Override
            public void onError(WebSocketChannel channel, Void context, Throwable throwable) {
                log.error("error on echo-back", throwable);
            }
        });
    }

    private void send(Message message, WebSocketChannel channel, WebSocketCallback<Void> callback) {
        final String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        WebSockets.sendText(json, channel, callback);
    }

    public void onClose(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        log.info("close");
    }

    @Value
    @RequiredArgsConstructor(onConstructor = @__({ @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) }))
    public static class Message {
        private final String message;
        private final Instant timestamp;
    }
}
