package stud.mi.server;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import stud.mi.server.channel.Channel;

public class ChatServer extends WebSocketServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);
    private static final JsonParser JSON_PARSER = new JsonParser();

    private static final String USER_TYPE = "USER";
    private static final String CHANNEL_MESSAGE = "CHANNEL_MSG";
    private static final String CHANNEL_JOIN_TYPE = "CHANNEL_JOIN";
    private static final String CHANNEL_EXIT_TYPE = "CHANNEL_EXIT";

    private static final Set<User> USER_LIST = new HashSet<>();
    private static final Set<Channel> CHANNEL_LIST = new HashSet<>();

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
        parseMessage(conn, message);
    }

    @Override
    public void onMessage(final WebSocket conn, final ByteBuffer message) {
        LOGGER.debug("Message received from {}, byte content size '{}'", conn.getRemoteSocketAddress().getHostName(),
                message.remaining());
    }

    @Override
    public void onError(final WebSocket conn, final Exception ex) {
        LOGGER.error("Error on Connection.", ex);
    }

    private void parseMessage(final WebSocket conn, final String message) {
        final JsonObject msg = JSON_PARSER.parse(message).getAsJsonObject();
        final String type = msg.get("type").getAsString();
        final String content = msg.get("content").getAsString();

        switch (type) {
        case USER_TYPE:
            final User newUser = new User(content, conn);
            final boolean alreadyAdded = USER_LIST.add(newUser);
            LOGGER.debug("User {} already added: {}", newUser.getName(), alreadyAdded);
            break;
        case CHANNEL_EXIT_TYPE:
            userExitChannel(conn, content);
            break;
        case CHANNEL_JOIN_TYPE:
            userJoinChannel(conn, content);
            break;
        case CHANNEL_MESSAGE:
            sendMessageToChannel(conn, content);
            break;
        default:
            LOGGER.debug("Message Type unknown: {}", type);
        }
    }

    private void sendMessageToChannel(final WebSocket userConnection, final String channelName) {

    }

    private void userExitChannel(final WebSocket userConnection, final String channelName) {
        final Channel channel = getChannel(channelName);
        if (channel != null) {
            final User user = getUser(userConnection);
            channel.userExit(user);
        }
    }

    private void userJoinChannel(final WebSocket userConnection, final String channelName) {
        final Channel channel = getChannel(channelName);
        if (channel != null) {
            final User user = getUser(userConnection);
            channel.userJoin(user);
        }
    }

    private User getUser(final WebSocket connection) {
        final Optional<User> optionalUser = USER_LIST.stream().filter(user -> user.getConnection().equals(connection))
                .findFirst();
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        }
        return null;
    }

    private User getUser(final String userName) {
        final Optional<User> optionalUser = USER_LIST.stream().filter(user -> user.getName().equals(userName))
                .findFirst();
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        }
        return null;
    }

    private Channel getChannel(final String channelName) {
        final Optional<Channel> optionalChannel = CHANNEL_LIST.stream()
                .filter(channel -> channel.getName().equalsIgnoreCase(channelName)).findFirst();
        if (optionalChannel.isPresent()) {
            return optionalChannel.get();
        }
        return null;
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
