package jp.k_ui.test.chat.controllers;

import static io.undertow.util.StatusCodes.BAD_REQUEST;

import org.springframework.stereotype.Component;

import io.undertow.server.HttpHandler;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Component
@Slf4j
public class ChatController implements Controller {
    @Override
    public String getPath() {
        return "/chat";
    }

    @Override
    public HttpHandler getHttpHandler() {
        return exchange -> {
            val queryParams = exchange.getQueryParameters();
            val userName = queryParams.get("user-name").getFirst();
            val roomName = queryParams.get("room-name").getFirst();
            if (userName == null || roomName == null) {
                exchange.setStatusCode(BAD_REQUEST);
                exchange.getResponseSender().send("Illegal params");
                return;
            }
            exchange.getResponseSender().send(
                    String.format(
                            "<script src=\"js/chat.js\"></script>\n"
                            + "<p>User Name: %s</p>\n"
                            + "<p>Room Name: %s</p>\n"
                            + "<form id=\"send-form\">\n"
                            + "  <input type=\"submit\">\n"
                            + "  <label><input type=\"text\"></label>\n"
                            + "</form>\n"
                            + "<table id=\"messages\" border=\"1\">\n"
                            + "  <tr>\n"
                            + "    <th>Timestamp</th>\n"
                            + "    <th>Type</th>\n"
                            + "    <th>Log</th>\n"
                            + "  </tr>\n"
                            + "</table>\n",
                            userName, roomName));
        };
    }
}
