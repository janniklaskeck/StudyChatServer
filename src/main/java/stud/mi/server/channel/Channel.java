package stud.mi.server.channel;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import stud.mi.message.Message;
import stud.mi.message.MessageType;
import stud.mi.server.user.RemoteUser;
import stud.mi.util.MessageBuilder;

public class Channel {

    private static final Logger LOGGER = LoggerFactory.getLogger(Channel.class);
    private static final int MAX_USERS = 512;
    private static final int DEFAULT_MAX_USERS = 64;

    private final List<RemoteUser> userList = new ArrayList<>();
    private String name;
    private int maxUsers;

    public Channel(final String name, final int maxUsers) {
        this.name = name;
        if (maxUsers > MAX_USERS) {
            this.maxUsers = MAX_USERS;
        } else if (maxUsers <= 1) {
            this.maxUsers = DEFAULT_MAX_USERS;
        } else {
            this.maxUsers = maxUsers;
        }
        LOGGER.debug("Channel created with name {} and {} maximum Users", this.name, this.maxUsers);
    }

    public Channel(final String name) {
        this(name, 64);
    }

    public void sendMessageToChannel(final RemoteUser sender, final String type) {
        LOGGER.debug("Sending message to channel {} with type {}", this.getName(), type);
        final JsonObject jo = MessageBuilder.buildMessageBaseJson(type);
        final Message customMessage = new Message(jo);
        this.sendMessageToChannel(sender, customMessage);
    }

    public void sendMessageToChannel(final RemoteUser sender, final Message message) {
        LOGGER.debug("Sending message to channel {} with type {}", this.getName(), message.getType());
        if (!userList.contains(sender)) {
            LOGGER.debug("User {} not member of Channel {} tried to send Message {}", sender.getName(), this.name,
                    message);
            return;
        }
        Message msg = null;
        switch (message.getType()) {
        case MessageType.CHANNEL_USER_JOIN:
            msg = MessageBuilder.buildUserJoinMessage(userList, this);
            break;
        case MessageType.CHANNEL_MESSAGE:
            msg = MessageBuilder.buildMessagePropagateAnswer(message.getMessage(), sender.getName());
            break;
        default:
            msg = new Message("{}");
            LOGGER.error("Tried to send message to channel with unknown type: {}", msg.getType());
            break;
        }
        for (final RemoteUser user : userList) {
            user.getConnection().send(msg.toJson());
        }
        LOGGER.debug("Message sent to channel {} with {} users, Content: {}", this.name, this.userList.size(),
                message.toJson());
    }

    public boolean userJoin(final RemoteUser user) {
        if (userList.size() < this.maxUsers) {
            userList.add(user);
            sendMessageToChannel(user, MessageType.CHANNEL_USER_JOIN);
            return true;
        }
        return false;
    }

    public boolean userExit(final RemoteUser user) {
        final boolean success = userList.remove(user);
        if (userList.isEmpty()) {
            ChannelRegistry.getInstance().removeChannel(this);
        }
        return success;
    }

    public List<RemoteUser> getUserList() {
        return userList;
    }

    public int getMaxUsers() {
        return maxUsers;
    }

    public String getName() {
        return name;
    }
}
