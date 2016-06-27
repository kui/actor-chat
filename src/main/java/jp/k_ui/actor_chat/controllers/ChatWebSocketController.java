package jp.k_ui.actor_chat.controllers;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import jp.k_ui.actor_chat.services.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor(onConstructor = @__({ @Autowired }))
@Slf4j
public class ChatWebSocketController implements Controller {
    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    @Override
    public String getPath() {
        return "chat/ws";
    }

    @Override
    public HttpHandler getHttpHandler() {
        return Handlers.websocket((exchange, channel) -> {
            Map<String, List<String>> queryParams = exchange.getRequestParameters();
            List<String> userNames = queryParams.get("user_name");
            ChatService.User user = new ChatService.User(userNames.get(0));
            List<String> roomNames = queryParams.get("room_name");
            ChatService.Room room = new ChatService.Room(roomNames.get(0));

            ChatService.SessionContext context =
                    new ChatService.SessionContext(exchange, user, room, new ChatService.Channel() {
                        @Override
                        public void send(Object o) throws IOException {
                            String json = objectMapper.writeValueAsString(o);
                            WebSockets.sendText(json, channel, null);
                        }

                        @Override
                        public void close() throws IOException {
                            channel.close();
                        }
                    });

            chatService.onConnect(context, () -> {
                channel.getReceiveSetter().set(new AbstractReceiveListener() {
                    @Override
                    protected void onFullTextMessage(WebSocketChannel channel,
                                                     BufferedTextMessage bufferedTextMessage) {
                        Message message;
                        try {
                            message = objectMapper.readValue(bufferedTextMessage.getData(), Message.class);
                        } catch (IOException e) {
                            log.warn("Message deserialize error", e);
                            return;
                        }
                        ChatService.Message chatServiceMessage =
                                new ChatService.Message(message.getTimestamp(), message.getMessage());
                        chatService.onReceive(context, chatServiceMessage);
                    }
                });
                channel.addCloseTask(channel1 -> chatService.onClose(context));
                channel.resumeReceives();
            });
        });
    }

    @Value
    @RequiredArgsConstructor
    public static class Message {
        private final Instant timestamp;
        private final String message;
    }
}
