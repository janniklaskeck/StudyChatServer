package stud.mi.server;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stud.mi.message.Message;
import stud.mi.message.MessageType;
import stud.mi.server.channel.Channel;
import stud.mi.server.channel.ChannelRegistry;
import stud.mi.server.user.RemoteUser;
import stud.mi.server.user.UserEvents;
import stud.mi.server.user.UserRegistry;

public final class ChatServer extends WebSocketServer
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);
    public static final int PROTOCOL_VERSION = 1;

    public ChatServer(final int port)
    {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onClose(final WebSocket conn, final int code, final String reason, final boolean remote)
    {
        ChatServer.LOGGER.debug("Connection to {} closed with code {} and Reason {}. Origin: {}", conn.getRemoteSocketAddress().getHostName(), code,
                reason, remote);
        UserRegistry.getInstance().removeUser(conn);
    }

    @Override
    public void onError(final WebSocket conn, final Exception e)
    {
        ChatServer.LOGGER.error("Error on Connection.", e);
    }

    @Override
    public void onMessage(final WebSocket conn, final ByteBuffer message)
    {
        ChatServer.LOGGER.debug("Message received from {}, byte content size '{}'", conn.getRemoteSocketAddress().getHostName(), message.remaining());
    }

    @Override
    public void onMessage(final WebSocket conn, final String message)
    {
        ChatServer.LOGGER.trace("Message received from {}, content '{}'", conn.getRemoteSocketAddress().getHostName(), message);
        parseMessage(conn, message);
    }

    @Override
    public void onOpen(final WebSocket conn, final ClientHandshake handshake)
    {
        ChatServer.LOGGER.debug("Connection from {}.", conn.getRemoteSocketAddress().getHostName());
    }

    private static void parseMessage(final WebSocket conn, final String message)
    {
        final UserRegistry userRegistry = UserRegistry.getInstance();
        final ChannelRegistry channelRegistry = ChannelRegistry.getInstance();
        final Message msg = new Message(message);
        switch (msg.getType())
        {
        case MessageType.USER_JOIN:
            final RemoteUser remoteUser = userRegistry.registerUser(conn, msg);
            channelRegistry.sendChannelsToUser(remoteUser);
            break;
        case MessageType.CHANNEL_JOIN:
            final Channel channel = channelRegistry.getChannel(msg.getChannelName());
            userRegistry.getUser(msg.getUserID()).getStateMachine().processEvent(UserEvents.JOIN_CHANNEL, channel);
            break;
        case MessageType.CHANNEL_MESSAGE:
            final RemoteUser user = userRegistry.getUser(msg.getUserID());
            user.getJointChannel().sendMessageToChannel(user, msg);
            break;
        case MessageType.USER_HEARTBEAT:
            userRegistry.getUser(msg.getUserID()).heartBeat();
            break;
        default:
            ChatServer.LOGGER.debug("Message Type unknown: {}", msg.getType());
        }
    }

}
