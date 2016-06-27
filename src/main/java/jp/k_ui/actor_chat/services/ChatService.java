package jp.k_ui.actor_chat.services;

import java.io.IOException;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import akka.actor.ActorRef;
import akka.actor.Inbox;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import jp.k_ui.actor_chat.akka.actors.WebSocketActor;
import jp.k_ui.actor_chat.akka.actors.ChatActor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * a glue for ActorSystem and Controller
 */
@Component
@Slf4j
public class ChatService {
    private final Inbox inbox;
    private final ActorRef chatActor;

    @Autowired
    public ChatService(Inbox inbox,
                       @Qualifier("chatActorRef") ActorRef chatActor) {
        this.inbox = inbox;
        this.chatActor = chatActor;
    }

    public void onConnect(SessionContext context, Runnable callback) {
        log.info("{} enters {}", context.getUser(), context.getRoom());
        inbox.send(chatActor, new ChatActor.EnterRoom(
                new ChatActor.User(context.getUser().getName()),
                new ChannelAdaptor(context.getChannel()),
                new ChatActor.Room(context.getRoom().getName()),
                Instant.now()));
        callback.run();
    }

    public void onReceive(SessionContext context, Message message) {
        log.debug("send message: message={}, context={}", message, context);
        inbox.send(chatActor, new ChatActor.BroadcastRoom(
                new ChatActor.User(context.getUser().getName()),
                new ChatActor.Room(context.getRoom().getName()),
                message.getMessage(),
                message.getTimestamp()
        ));
    }

    public void onClose(SessionContext context) {
        log.info("{} leaves {}", context.getUser(), context.getRoom());
        inbox.send(chatActor, new ChatActor.DeleteUser(
                new ChatActor.User(context.getUser().getName()),
                new ChatActor.Room(context.getRoom().getName()),
                Instant.now()
        ));
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
        private User user;
        @NonNull
        private Room room;
        @NonNull
        private Channel channel;
    }

    public interface Channel {
        void send(Object o) throws IOException;

        void close() throws IOException;
    }

    @RequiredArgsConstructor
    public static class ChannelAdaptor implements WebSocketActor.Channel {
        private final Channel channel;

        @Override
        public void send(Object o) throws IOException {
            channel.send(o);
        }

        @Override
        public void close() throws IOException {
            channel.close();
        }
    }
}
