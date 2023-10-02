package dev.relismdev.rcore.messages;

import dev.relismdev.rcore.api.contexts.sseContext;
import dev.relismdev.rcore.api.socketHandler;
import org.apache.logging.log4j.Level;
import io.socket.client.Socket;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.json.JSONObject;

@Plugin(name = "Log4JAppender", category = "Core", elementType = "appender", printObject = true)
public class consoleListener extends AbstractAppender {

    private Socket socket = null;
    private sseContext sseContext = null;

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
        // Check if socket is initialized
        if (socket == null && socketHandler.socket != null) {
            socket = socketHandler.socket;
        }

        // Check if sseConsoleStream is initialized
        if (sseContext == null) {
            sseContext = new sseContext();
        }

        // Extract log message data
        String message = event.getMessage().getFormattedMessage();
        Level level = event.getLevel();

        // Build the logData JSON object
        JSONObject logData = new JSONObject();
        logData.put("message", message);
        logData.put("level", level.toString());

        // Emit events only if socket and sseConsoleStream are initialized
        if (socket != null) {
            socket.emit("forward", "dashboard", "console-incoming", logData);
            socket.emit("forward", "rcore-ds", "console-incoming", logData);
        }

        if (sseContext != null) {
            sseContext.sendEvent("console-message", logData);
        }
    }
}
