package stud.mi.server;

import java.net.InetSocketAddress;
import java.util.Collection;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatServer extends WebSocketServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);

    public ChatServer(final int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(final WebSocket conn, final ClientHandshake handshake) {
        LOGGER.debug("Connection from {}.", conn.getRemoteSocketAddress().getHostName());
    }

    @Override
    public void onClose(final WebSocket conn, final int code, final String reason, final boolean remote) {
        LOGGER.debug("Connection to {} closed with code {} and Reason {}. Origin: {}",
                conn.getRemoteSocketAddress().getHostName(), code, reason, remote);
    }

    @Override
    public void onMessage(final WebSocket conn, final String message) {
        LOGGER.debug("Message received from {}, content '{}'", conn.getRemoteSocketAddress().getHostName(), message);
        sendToOthers(conn, message);
    }

    @Override
    public void onError(final WebSocket conn, final Exception ex) {
        LOGGER.error("Error on Connection.", ex);
    }

    private void sendToOthers(final WebSocket source, final String message) {
        for (final WebSocket conn : connections()) {
            if (!conn.equals(source)) {
                conn.send(message);
            }
        }
    }

    public static void sendToAll(WebSocketServer wss, String text) {
        Collection<WebSocket> con = wss.connections();
        synchronized (con) {
            for (WebSocket c : con) {
                c.send(text);
            }
        }
    }

}
