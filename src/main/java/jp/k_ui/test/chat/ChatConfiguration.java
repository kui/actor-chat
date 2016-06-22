package jp.k_ui.test.chat;

import static jp.k_ui.test.chat.akka.SpringExtension.SpringExtensionProvider;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import akka.actor.ActorSystem;
import lombok.val;

@Configuration
public class ChatConfiguration {
    @Bean
    public ActorSystem actorSystem(ApplicationContext applicationContext) {
        ActorSystem actorSystem = ActorSystem.create("chat-actor-system");
        SpringExtensionProvider.get(actorSystem).init(applicationContext);
        return actorSystem;
    }

    @Bean
    public ObjectMapper objectMapper() {
        val om = new ObjectMapper();
        om.findAndRegisterModules();
        om.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
        om.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
        return om;
    }
}
