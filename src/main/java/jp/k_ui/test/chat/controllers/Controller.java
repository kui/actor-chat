package jp.k_ui.test.chat.controllers;

import io.undertow.server.HttpHandler;

public interface Controller {
    String getPath();

    HttpHandler getHttpHandler();
}
