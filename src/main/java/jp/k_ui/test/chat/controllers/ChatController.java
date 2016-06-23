package jp.k_ui.test.chat.controllers;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Deque;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import io.undertow.server.HttpHandler;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

@Component
public class ChatController implements Controller {
    private static final Pattern ROOM_NAME_PATTERN = Pattern.compile("\\Q{{roomName}}\\E");
    private static final Pattern USER_NAME_PATTERN = Pattern.compile("\\Q{{userName}}\\E");

    private static final File TEMPLATE_FILE;

    static {
        try {
            ClassPathResource classPathResource = new ClassPathResource("template/chat.html");
            TEMPLATE_FILE = classPathResource.getFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String TEMPLATE;

    static {
        try {
            TEMPLATE = new String(Files.readAllBytes(TEMPLATE_FILE.toPath()), UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getPath() {
        return "chat";
    }

    @Override
    public HttpHandler getHttpHandler() {
        return exchange -> {
            Map<String, Deque<String>> queryParams = exchange.getQueryParameters();
            Deque<String> userName = queryParams.get("user_name");
            Deque<String> roomName = queryParams.get("room_name");

            if (userName == null || userName.isEmpty()
                || roomName == null || roomName.isEmpty()) {
                exchange.setStatusCode(StatusCodes.BAD_REQUEST);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                exchange.getResponseSender().send("Illegal params");
                return;
            }

            String content = TEMPLATE;
            content = USER_NAME_PATTERN.matcher(content).replaceAll(userName.getFirst());
            content = ROOM_NAME_PATTERN.matcher(content).replaceAll(roomName.getFirst());

            exchange.getResponseSender().send(content, UTF_8);
        };
    }
}
