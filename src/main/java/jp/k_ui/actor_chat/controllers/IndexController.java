package jp.k_ui.actor_chat.controllers;

import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;

@Component
public class IndexController implements Controller {
    @Override
    public String getPath() {
        return "/";
    }

    @Override
    public HttpHandler getHttpHandler() {
        return Handlers.resource(new ClassPathResourceManager(ClassUtils.getDefaultClassLoader(), "static"));
    }
}
