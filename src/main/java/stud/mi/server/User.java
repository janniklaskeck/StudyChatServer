package stud.mi.server;

import org.java_websocket.WebSocket;

public class User {

    private String name;
    private WebSocket connection;

    public User(final String name, final WebSocket connection) {
        this.name = name;
        this.connection = connection;
    }

    public String getName() {
        return this.name;
    }

    public WebSocket getConnection() {
        return this.connection;
    }
}
