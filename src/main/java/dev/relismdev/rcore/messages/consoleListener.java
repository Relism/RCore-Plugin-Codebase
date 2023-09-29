package dev.relismdev.rcore.messages;

import dev.relismdev.rcore.api.socketHandler;
import dev.relismdev.rcore.storage.localStorage;
import dev.relismdev.rcore.storage.loggerStorage;
import dev.relismdev.rcore.utils.msg;
import org.apache.logging.log4j.Level;
import io.socket.client.Socket;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Plugin(name = "Log4JAppender", category = "Core", elementType = "appender", printObject = true)
public class consoleListener extends AbstractAppender {

    Socket socket = socketHandler.socket;

    private ExecutorService executor = Executors.newFixedThreadPool(10);
    loggerStorage lgs = new loggerStorage();
    localStorage ls = new localStorage();

    public consoleListener() {
        super("Log4JAppender", null,
                PatternLayout.newBuilder()
                        .withPattern("[%d{HH:mm:ss} %level]: %msg")
                        .build(), false);
    }

    @Override
    public boolean isStarted() {
        return true;
    }

    @Override
    public void append(LogEvent event) {
        // Extract log message data
        String message = event.getMessage().getFormattedMessage();
        Level level = event.getLevel();

        // Build the logData JSON object
        JSONObject logData = new JSONObject();
        logData.put("message", message);
        logData.put("level", level.toString());

        socket.emit("forward", "dashboard", "console-incoming", logData);
        socket.emit("forward", "rcore-ds", "console-incoming", logData);
    }
}