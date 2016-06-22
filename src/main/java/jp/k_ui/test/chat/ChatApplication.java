package jp.k_ui.test.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.Setter;

@SpringBootApplication
public class ChatApplication implements CommandLineRunner {
    @Autowired
    @Setter
    private ChatServer chatServer;

    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        chatServer.start();
    }
}
