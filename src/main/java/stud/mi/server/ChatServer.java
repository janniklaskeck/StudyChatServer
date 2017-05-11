package stud.mi.server;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stud.mi.message.Message;
import stud.mi.message.MessageType;
import stud.mi.server.channel.Channel;
import stud.mi.util.MessageBuilder;

public class ChatServer extends WebSocketServer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);

	private static final Map<Long, User> USER_LIST = new HashMap<>();
	private static final Set<Channel> CHANNEL_LIST = new HashSet<>();
	public static final int PROTOCOL_VERSION = 1;

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
		final User user = getUser(conn);
		user.exitChannel();
		USER_LIST.remove(user.getID());
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
	public void onError(final WebSocket conn, final Exception e) {
		LOGGER.error("Error on Connection.", e);
	}

	private void parseMessage(final WebSocket conn, final String message) {
		final Message msg = new Message(message);
		switch (msg.getType()) {
		case MessageType.USER_JOIN:
			registerUser(conn, msg);
			break;
		case MessageType.CHANNEL_JOIN:
			final Channel channel = addChannel(msg.getChannelName());
			final User user = getUser(msg.getUserID());
			user.joinChannel(channel);
			break;
		default:
			LOGGER.debug("Message Type unknown: {}", "");
		}
	}

	private User getUser(final Long userID) {
		final User user = USER_LIST.entrySet().stream().filter(entry -> entry.getValue().getID().equals(userID))
				.collect(Collectors.toList()).get(0).getValue();
		LOGGER.trace("Return the User {} for ID {}", user, userID);
		return user;
	}

	public static void removeChannel(final Channel channel) {
		CHANNEL_LIST.remove(channel);
		LOGGER.debug("Channel {} removed.", channel.getName());
	}

	private void registerUser(final WebSocket connection, final Message msg) {
		final boolean userAlreadyExists = USER_LIST.entrySet().stream()
				.filter(entry -> entry.getValue().getName().equalsIgnoreCase(msg.getUserName()))
				.collect(Collectors.toList()).isEmpty();
		if (userAlreadyExists) {
			final Message reply = MessageBuilder.buildUserJoinAnswer(-1L);
			connection.send(reply.toJson());
			LOGGER.debug("User {} already registered.", msg.getUserName());
			return;
		}
		final Long newUserID = generateUserID();
		final User user = new User(connection, msg.getUserName(), 0L);
		USER_LIST.put(newUserID, user);
		LOGGER.debug("Registered new User with Name {} and ID {}", user.getName(), user.getID());
	}

	private Long generateUserID() {
		final Random rnd = new Random();
		Long userID = rnd.nextLong();
		while (USER_LIST.containsKey(userID)) {
			userID = rnd.nextLong();
		}
		LOGGER.trace("Return new User ID {}.", userID);
		return userID;
	}

	public static Channel addChannel(final String channelName) {
		final Channel newChannel = new Channel(channelName);
		if (!CHANNEL_LIST.add(newChannel)) {
			LOGGER.debug("Channel {} already exists and was not added.", channelName);
			return getChannel(channelName);
		}
		LOGGER.debug("Added new Channel {}.", channelName);
		return newChannel;
	}

	private static Channel getChannel(final String channelName) {
		return CHANNEL_LIST.stream().filter(channel -> channel.getName().equalsIgnoreCase(channelName))
				.collect(Collectors.toList()).get(0);
	}

	private static User getUser(final WebSocket connection) {
		return USER_LIST.entrySet().stream().filter(entry -> entry.getValue().getConnection().equals(connection))
				.collect(Collectors.toList()).get(0).getValue();
	}

}
