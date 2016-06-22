package jp.k_ui.test.chat.controllers;

import java.io.IOException;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import jp.k_ui.test.chat.services.ChatService;
import jp.k_ui.test.chat.services.EchoBackService;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Autowired }))
public class ChatWebSocketController implements Controller {
    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    @Override
    public String getPath() {
        return "/chat/ws";
    }

    @Override
    public HttpHandler getHttpHandler() {
        return Handlers.websocket((exchange, channel) -> chatService.onConnect(exchange, channel, err -> {
            channel.getReceiveSetter().set(new AbstractReceiveListener() {
                @Override
                protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
                    String messageString = message.getData();
                    log.info("receive json: {}", messageString);
                    ChatMessage chatMessage;
                    try {
                        chatMessage = objectMapper.readValue(messageString, ChatMessage.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    chatService.onReceive(exchange, channel, chatMessage);
                }
            });
            channel.addCloseTask(c -> chatService.onClose(exchange, c));
            channel.resumeReceives();
        }));
    }

    @Value
    @RequiredArgsConstructor(onConstructor = @__({ @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) }))
    public static class ChatMessage {
        private final String message;
        private final Instant timestamp;
    }
}
