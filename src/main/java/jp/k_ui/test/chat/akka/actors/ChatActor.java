package jp.k_ui.test.chat.akka.actors;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.japi.pf.ReceiveBuilder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChatActor extends AbstractActor {
    private final Map<Room, ActorRef> rooms = new HashMap<>();

    public ChatActor() {
        receive(ReceiveBuilder.match(EnterRoom.class, this::handle)
                              .match(DeleteUser.class, this::tellRoom)
                              .match(BroadcastRoom.class, this::tellRoom)
                              .match(Terminated.class, this::handle)
                              .build());
    }

    public static Props props() {
        return Props.create(ChatActor.class);
    }

    private void handle(Terminated terminated) {
        Room room = rooms.entrySet().stream()
                         .filter(e -> e.getValue().equals(terminated.getActor()))
                         .findFirst()
                         .map(Map.Entry::getKey)
                         .orElseThrow(() -> new IllegalStateException("No such room"));
        rooms.remove(room);
        log.info("Remove room: {}", room);
    }

    private void handle(EnterRoom enterRoom) {
        ActorRef roomActor = rooms.computeIfAbsent(enterRoom.getRoom(), r -> createRoom(enterRoom));
        ActorRef websocketActor = context().actorOf(WebSocketActor.props(enterRoom.getChannel()));
        roomActor.tell(new JoinUser(enterRoom.getUser(), websocketActor, enterRoom.getTimestamp()), self());
    }

    private ActorRef createRoom(EnterRoom enterRoom) {
        log.info("Create room: {}", enterRoom);
        ActorRef roomActor = context().actorOf(RoomActor.props(enterRoom.getRoom()));
        context().watch(roomActor);
        return roomActor;
    }

    private <T extends RoomTask> void tellRoom(T roomTask) {
        ActorRef roomActor = rooms.get(roomTask.getRoom());
        if (roomActor == null) {
            log.warn("No such room: {}", roomTask.getRoom());
            return;
        }
        roomActor.tell(roomTask, self());
    }

    interface ChatUpdate {
        From getFrom();

        Instant getTimestamp();
    }

    interface ChatLogElement extends ChatUpdate {
        String getMessage();
    }

    @FunctionalInterface
    interface RoomTask {
        Room getRoom();
    }

    @FunctionalInterface
    interface From {
        From SYSTEM = () -> "system";

        String getName();
    }

    @Value
    @EqualsAndHashCode(of = "name")
    public static class Room {
        @NonNull
        private final String name;
    }

    @Value
    @EqualsAndHashCode(of = "name")
    public static class User implements From {
        @NonNull
        private final String name;
    }

    @Value
    @RequiredArgsConstructor
    public static class BroadcastRoom implements RoomTask, ChatLogElement {
        @NonNull
        private final From from;
        @NonNull
        private final Room room;
        @NonNull
        private final String message;
        @NonNull
        private final Instant timestamp;
    }

    @Value
    public static class EnterRoom implements RoomTask {
        @NonNull
        private final User user;
        @NonNull
        private WebSocketActor.Channel channel;
        @NonNull
        private final Room room;
        @NonNull
        private final Instant timestamp;
    }

    @Value
    public static class JoinUser {
        @NonNull
        private final User user;
        @NonNull
        private final ActorRef websocketActor;
        @NonNull
        private final Instant timestamp;
    }

    @Value
    public static class DeleteUser implements RoomTask {
        @NonNull
        private final User user;
        @NonNull
        private final Room room;
        @NonNull
        private final Instant timestamp;
    }
}
