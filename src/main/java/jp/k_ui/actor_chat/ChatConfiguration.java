package jp.k_ui.actor_chat;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Inbox;
import jp.k_ui.actor_chat.akka.SpringExtension;
import jp.k_ui.actor_chat.akka.actors.ChatActor;
import lombok.val;

@Configuration
public class ChatConfiguration {
    @Bean
    public ObjectMapper objectMapper() {
        val om = new ObjectMapper();
        om.findAndRegisterModules();
        om.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
        om.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
        return om;
    }

    @Bean
    public ActorRef chatActorRef(ActorSystem actorSystem) {
        return actorSystem.actorOf(ChatActor.props());
    }

    @Bean
    public ActorSystem actorSystem(ApplicationContext ac) {
        val as = ActorSystem.create("chat");
        SpringExtension.SpringExtensionProvider.get(as).init(ac);
        return as;
    }

    @Bean
    public Inbox inbox(ActorSystem actorSystem) {
        return Inbox.create(actorSystem);
    }
}
