package jp.k_ui.actor_chat.akka.actors;

import java.io.IOException;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebSocketActor extends AbstractActor {
    private final Channel channel;

    public WebSocketActor(Channel channel) {
        this.channel = channel;

        receive(ReceiveBuilder.match(ChatActor.ChatUpdate.class, this::send)
                              .build());
    }

    private void send(Object o) throws IOException {
        channel.send(o);
    }

    public static Props props(Channel channel) {
        return Props.create(WebSocketActor.class, channel);
    }

    @Override
    public void postStop() throws Exception {
        channel.close();
    }

    public interface Channel {
        void send(Object o) throws IOException;

        void close() throws IOException;
    }
}
