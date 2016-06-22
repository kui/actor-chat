package jp.k_ui.test.chat.akka.actors;

import static jp.k_ui.test.chat.akka.SpringExtension.SpringExtensionProvider;

import java.util.concurrent.Callable;

import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import io.undertow.websockets.core.WebSocketCallback;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import lombok.Value;

public class UserActor extends AbstractActor {
    private final User user;
    private final WebSocketChannel channel;
    private final ObjectMapper objectMapper;

    public UserActor(User user, WebSocketChannel channel, ObjectMapper objectMapper) {
        this.user = user;
        this.channel = channel;
        this.objectMapper = objectMapper;

        receive(ReceiveBuilder
                        .match(RoomActor.ChatMessage.class, message -> {
                            send(new ChatUserMessage(message.getSender(), message.getMessage()));
                        })
                        .match(RoomActor.SystemMessage.class, message -> {
                            send(new ChatSystemMessage(message.getMessage()));
                        })
                        .build());
    }

    public static Props props(User user, WebSocketChannel channel, ActorSystem system) {
        ApplicationContext applicationContext = SpringExtensionProvider.get(system).getApplicationContext();
        return Props.create(UserActor.class, () -> applicationContext.getBean(UserActor.class, user, channel));
    }

    private void send(Object message) {
        send(message, null);
    }

    private void send(Object message, WebSocketCallback<Void> callback) {
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        WebSockets.sendText(json, channel, callback);
    }

    @Value
    public static class User {
        private String name;
    }

    @SuppressWarnings("InterfaceMayBeAnnotatedFunctional")
    public interface ChatEntity {
        ChatEntityType getType();
        String getMessage();
    }

    @Value
    public static class ChatUserMessage implements ChatEntity {
        private final ChatEntityType type = ChatEntityType.USER_MESSAGE;
        private final User user;
        private final String message;
    }

    @Value
    public static class ChatSystemMessage implements ChatEntity {
        private final ChatEntityType type = ChatEntityType.SYSTEM_MESSAGE;
        private final String message;
    }

    public enum ChatEntityType {
        USER_MESSAGE, SYSTEM_MESSAGE;
    }
}
