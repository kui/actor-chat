package jp.k_ui.actor_chat.akka.actors;

import static java.lang.String.format;

import java.time.Instant;
import java.util.HashMap;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RoomActor extends AbstractActor {
    private final ChatActor.Room room;
    private final HashMap<ChatActor.User, ActorRef> users = new HashMap<>();

    public RoomActor(ChatActor.Room room) {
        this.room = room;

        receive(ReceiveBuilder.match(ChatActor.BroadcastRoom.class, this::tellAllUser)
                              .match(ChatActor.JoinUser.class, this::createUser)
                              .match(ChatActor.DeleteUser.class, this::deleteUser)
                              .build());
    }

    public static Props props(ChatActor.Room room) {
        return Props.create(RoomActor.class, () -> new RoomActor(room));
    }

    private void tellAllUser(ChatActor.ChatLogElement message) {
        log.debug("Broadcast in {}: {}", room, message);
        users.values().forEach(u -> u.tell(message, self()));
    }

    private void createUser(ChatActor.JoinUser joinUser) {
        log.info("Create user in {}: {}", room, joinUser.getUser());

        if (users.containsKey(joinUser.getUser())) {
            Warn warn = new Warn(format("Error: User \"%s\" already exits", joinUser.getUser().getName()));
            joinUser.getWebsocketActor().tell(warn, self());
            joinUser.getWebsocketActor().tell(PoisonPill.getInstance(), self());
            return;
        }

        ActorRef userActor = context().actorOf(UserActor.props(joinUser.getUser(),
                                                               joinUser.getWebsocketActor()));
        users.put(joinUser.getUser(), userActor);

        tellAllUser(new SystemMessage(format("\"%s\" entered.", joinUser.getUser().getName())));

    }

    private void deleteUser(ChatActor.DeleteUser removeUser) {
        log.info("Delete user in {}: {}", room, removeUser.getUser());

        ActorRef userActor = users.get(removeUser.getUser());
        if (userActor == null) {
            return;
        }

        tellAllUser(new SystemMessage(format("\"%s\" leaved.", removeUser.getUser().getName())));

        userActor.tell(PoisonPill.getInstance(), self());
        users.remove(removeUser.getUser());
    }

    @Value
    public static class SystemMessage implements ChatActor.ChatLogElement {
        @NonNull
        private final ChatActor.From from = ChatActor.From.SYSTEM;
        @NonNull
        private final String message;
        @NonNull
        private final Instant timestamp = Instant.now();

        public SystemMessage(String message) {
            this.message = message;
        }
    }

    @Value
    public static class Warn implements ChatActor.ChatLogElement {
        @NonNull
        private final ChatActor.From from = ChatActor.From.SYSTEM;
        @NonNull
        private final String message;
        @NonNull
        private final Instant timestamp = Instant.now();
    }
}
