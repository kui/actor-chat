package jp.k_ui.actor_chat.controllers;

import io.undertow.server.HttpHandler;

public interface Controller {
    String getPath();

    HttpHandler getHttpHandler();
}
