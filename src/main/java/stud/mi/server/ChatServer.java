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

import com.google.gson.Gson;

import stud.mi.server.channel.Channel;

public class ChatServer extends WebSocketServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);

    private static final String CHANNEL_MESSAGE = "CHANNEL_MSG";
    private static final String CHANNEL_JOIN_TYPE = "CHANNEL_JOIN";
    private static final String CHANNEL_EXIT_TYPE = "CHANNEL_EXIT";

    private static final Set<User> USER_LIST = new HashSet<>();
    private static final Set<Channel> CHANNEL_LIST = new HashSet<>();
    private static final String USER_ALREADY_REGISTERED_MSG = "USER {} already registered on Server.";

    private static final Gson GSON = new Gson();

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
        final Message msg = GSON.fromJson(message, Message.class);
        switch (msg.getType()) {
        case CHANNEL_EXIT_TYPE:
            userExitChannel(conn, msg);
            break;
        case CHANNEL_JOIN_TYPE:
            userJoinChannel(conn, msg);
            break;
        case CHANNEL_MESSAGE:
            sendMessageToChannel(conn, msg);
            break;
        default:
            LOGGER.debug("Message Type unknown: {}", msg.getType());
        }
    }

    private void sendMessageToChannel(final WebSocket userConnection, final Message message) {
        final Channel channel = getChannel(message.getChannelName());
        if (channel != null) {
            final User user = getUser(userConnection);
            channel.sendMessageToChannel(user, message);
        } else {
            LOGGER.debug("Channel {} does not exist!", message.getChannelName());
        }
    }

    private void userExitChannel(final WebSocket userConnection, final Message message) {
        final Channel channel = getChannel(message.getChannelName());
        if (channel != null) {
            final User user = getUser(userConnection);
            channel.userExit(user);
        }
    }

    private void userJoinChannel(final WebSocket userConnection, final Message message) {
        Channel channel = getChannel(message.getChannelName());
        final User newUser = new User(message.getUserName(), userConnection);
        if (!USER_LIST.add(newUser)) {
            LOGGER.debug("User {} already added", newUser.getName());
            userConnection.close(1, String.format(USER_ALREADY_REGISTERED_MSG, message.getUserName()));
        }
        if (channel == null) {
            channel = addChannel(message.getChannelName());
        }
        final User user = getUser(userConnection);
        channel.userJoin(user);
    }

    private Channel addChannel(final String channelName) {
        final Channel newChannel = new Channel(channelName);
        CHANNEL_LIST.add(newChannel);
        return newChannel;
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

    public static void sendToAll(WebSocketServer wss, String text) {
        Collection<WebSocket> con = wss.connections();
        synchronized (con) {
            for (WebSocket c : con) {
                c.send(text);
            }
        }
    }
}
