package jp.k_ui.test.chat.controllers;

import org.springframework.stereotype.Component;

import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.websockets.core.WebSockets;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ChatWebSocketController implements Controller {
    @Override
    public String getPath() {
        return "chat/ws";
    }

    @Override
    public HttpHandler getHttpHandler() {
        return Handlers.websocket((exchange, channel) -> {
            log.info("query string: {}", exchange.getRequestParameters());
            WebSockets.sendText("{\"message\": \"Hello!\"}", channel, null);
            channel.resumeReceives();
        });
    }
}
