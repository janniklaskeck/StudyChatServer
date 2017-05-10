package stud.mi.server.channel;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stud.mi.server.Message;
import stud.mi.server.User;
import stud.mi.util.MessageComparator;

public class Channel {

    private static final Logger LOGGER = LoggerFactory.getLogger(Channel.class);
    private static final int MAX_USERS = 1024;
    private static final int DEFAULT_MAX_USERS = 64;

    private static final MessageComparator MESSAGE_COMPARATOR = new MessageComparator();

    private final List<User> userList = new ArrayList<>();
    private final List<Message> messageList = new ArrayList<>();
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

    public void sendMessageToChannel(final User user, final Message message) {
        if (!userList.contains(user)) {
            LOGGER.debug("User {} not member of Channel {} tried to send Message {}", user.getName(), this.name,
                    message.getContent());
            return;
        }
        messageList.add(message);
        messageList.sort(MESSAGE_COMPARATOR);
        LOGGER.debug("Message sent to channel {}, Content: {}", this.name, message);
    }

    public boolean userJoin(final User user) {
        if (userList.size() < this.maxUsers) {
            userList.add(user);
            return true;
        }
        return false;
    }

    public boolean userExit(final User user) {
        return userList.remove(user);
    }

    public List<User> getUserList() {
        return userList;
    }

    public int getMaxUsers() {
        return maxUsers;
    }

    public String getName() {
        return name;
    }
}
