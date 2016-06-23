package jp.k_ui.test.chat.services;

import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.spi.WebSocketHttpExchange;

@Component
public class ChatService {

    public void onClose(WebSocketHttpExchange exchange, WebSocketChannel channel) {
    }

    public void onReceive(WebSocketHttpExchange exchange,
                          WebSocketChannel channel,
                          Message message) {

    }

    public void onConnect(WebSocketHttpExchange exchange,
                          WebSocketChannel channel,
                          Consumer<Exception> callback) {
    }

    private class Message {
    }
}
