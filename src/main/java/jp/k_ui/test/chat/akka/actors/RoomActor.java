package jp.k_ui.test.chat.akka.actors;

import static java.lang.String.format;

import java.util.HashMap;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import io.undertow.websockets.core.WebSocketChannel;
import lombok.Value;

public class RoomActor extends AbstractActor {
    private final String name;
    private final HashMap<UserActor.User, ActorRef> users = new HashMap<>();

    public RoomActor(String name) {
        this.name = name;

        receive(ReceiveBuilder
                        .match(Join.class, join -> {
                            ActorRef userActor =
                                    context().actorOf(UserActor.props(join.getUser(),
                                                                      join.getChannel(),
                                                                      context().system()));
                            context().watch(userActor);
                            users.put(join.getUser(), userActor);
                            userActor.tell(new SystemMessage(format("Hello, %s! Welcome.",
                                                                    join.getUser().getName())),
                                           self());
                        })
                        .match(Leave.class, leave -> {
                            ActorRef userActor = users.get(leave.getUser());
                            userActor.tell(PoisonPill.getInstance(), self());
                        })
                        .match(ChatMessage.class, this::broadcast)
                        .build());
    }

    public static Props props(String name) {
        return Props.create(RoomActor.class, () -> new RoomActor(name));
    }

    private void broadcast(ChatMessage message) {
        users.values().forEach(userActor -> userActor.tell(message, self()));
    }

    @Value
    public static class Join {
        private UserActor.User user;
        private WebSocketChannel channel;
    }

    @Value
    public static class Leave {
        private UserActor.User user;
    }

    @Value
    public static class ChatMessage {
        private UserActor.User sender;
        private String message;
    }

    @Value
    public static class SystemMessage {
        private String message;
    }
}
