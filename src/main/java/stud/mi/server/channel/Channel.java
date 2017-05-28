package stud.mi.server.channel;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stud.mi.message.Message;
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

    public void sendMessageToChannel(final RemoteUser user, final Message message) {
        if (!userList.contains(user)) {
            LOGGER.debug("User {} not member of Channel {} tried to send Message {}", user.getName(), this.name,
                    message.getMessage());
            return;
        }
        sendMessageToOthers(user, message.getMessage());
        LOGGER.debug("Message sent to channel {}, Content: {}", this.name, message);
    }

    private void sendMessageToOthers(final RemoteUser sender, final String message) {
        for (final RemoteUser user : userList) {
            if (!user.getID().equals(sender.getID())) {
                final Message msg = MessageBuilder.buildMessagePropagateAnswer(message, sender.getName());
                user.getConnection().send(msg.toJson());
            }
        }
    }

    public boolean userJoin(final RemoteUser user) {
        if (userList.size() < this.maxUsers) {
            userList.add(user);
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
