package jp.k_ui.test.chat.services;

import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import jp.k_ui.test.chat.controllers.ChatWebSocketController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ChatService {
    
    public void onClose(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        log.info("close: {}", channel.getLocalAddress());
    }

    public void onReceive(WebSocketHttpExchange exchange,
                          WebSocketChannel channel,
                          ChatWebSocketController.ChatMessage chatMessage) {}

    public void onConnect(WebSocketHttpExchange exchange,
                          WebSocketChannel channel,
                          Consumer<Exception> callback) {

    }
}
