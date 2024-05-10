package com.daniu.config;

import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class ServerStartedListener implements ApplicationListener<WebServerInitializedEvent> {

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        int port = event.getWebServer().getPort();
        String localUrl = String.format("http://%s:%d", "localhost", port) + "/test";
        try {
            Runtime.getRuntime().exec("cmd /c start " + localUrl);
            System.out.println(localUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
