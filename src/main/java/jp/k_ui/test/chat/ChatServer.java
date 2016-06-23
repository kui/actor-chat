package jp.k_ui.test.chat;

import java.util.List;
import java.util.function.Function;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import com.google.common.collect.ImmutableList;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;
import jp.k_ui.test.chat.controllers.Controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Autowired }))
public class ChatServer implements DisposableBean {
    private final Config config;
    private final List<Controller> controllers;

    private Undertow undertow;

    public void start() {
        val pathHandler = new PathHandler();
        for (Controller c : controllers) {
            pathHandler.addPrefixPath(c.getPath(), c.getHttpHandler());
            log.info(" path \"{}\" -> {}", c.getPath(), c.getHttpHandler());
        }

        val filters = ImmutableList.<Function<HttpHandler, HttpHandler>>of(
                ChatServer::accessLoggerHandler);

        val handler = filters.stream()
                             .reduce((f, g) -> h -> g.apply(f.apply(h)))
                             .map(f -> f.apply(pathHandler))
                             .orElseThrow(RuntimeException::new);

        undertow = Undertow.builder()
                           .addHttpListener(config.getPort(), config.getHost(), handler)
                           .build();
        undertow.start();
        log.info("Server stop: Listening \"{}:{}\"", config.getHost(), config.getPort());
    }

    private static HttpHandler accessLoggerHandler(HttpHandler next) {
        val accessLogger = LoggerFactory.getLogger("accesslog");
        return new AccessLogHandler(
                next,
                accessLogger::info,
                "combined",
                ClassUtils.getDefaultClassLoader());
    }

    @Override
    public void destroy() throws Exception {
        undertow.stop();
        log.info("Server stop");
    }

    @Component
    @lombok.Value
    public static class Config {
        private final String host;
        private final int port;

        @Autowired
        public Config(@Value("${host:localhost}") String host,
                      @Value("${port:8080}") int port) {
            this.host = host;
            this.port = port;
        }
    }
}
