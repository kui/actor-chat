package jp.k_ui.actor_chat.akka.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

public class UserActor extends AbstractActor {
    private final ChatActor.User user;
    private final ActorRef webSocketActor;

    public UserActor(ChatActor.User user, ActorRef webSocketActor) {
        this.user = user;
        this.webSocketActor = webSocketActor;

        receive(ReceiveBuilder.match(ChatActor.ChatLogElement.class,
                                     message -> webSocketActor.tell(message, self()))
                              .build());
    }

    public static Props props(ChatActor.User user, ActorRef webSocketActor) {
        return Props.create(UserActor.class, user, webSocketActor);
    }

    @Override
    public void postStop() throws Exception {
        webSocketActor.tell(PoisonPill.getInstance(), self());
    }
}
