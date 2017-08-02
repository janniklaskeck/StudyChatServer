package stud.mi.server.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import stud.mi.message.Message;
import stud.mi.message.MessageType;
import stud.mi.server.user.RemoteUser;
import stud.mi.util.MessageBuilder;

public class Channel
{

    private static final Logger LOGGER = LoggerFactory.getLogger(Channel.class);
    private static final int MAX_USERS = 512;
    private static final int DEFAULT_MAX_USERS = 64;
    private static final String HISTORY_URL_TEMPLATE = "https://studychatbackend.mybluemix.net/history/channel/%s";
    private static final String HISTORY_URL_TEMPLATE_LOCAL = "http://127.0.0.1:8089/history/channel/%s";

    private static final SslContextFactory sslContextFactory = new SslContextFactory();
    private static final HttpClient client = new HttpClient(sslContextFactory);

    private final List<RemoteUser> userList = new ArrayList<>();
    private String name;
    private int maxUsers;

    public Channel(final String name, final int maxUsers)
    {
        this.name = name;
        if (maxUsers > MAX_USERS)
        {
            this.maxUsers = MAX_USERS;
        }
        else if (maxUsers <= 1)
        {
            this.maxUsers = DEFAULT_MAX_USERS;
        }
        else
        {
            this.maxUsers = maxUsers;
        }

        try
        {
            client.start();
        }
        catch (final Exception e)
        {
            LOGGER.error("Could not start HTTP Client.", e);
        }

        LOGGER.debug("Channel created with name {} and {} maximum Users", this.name, this.maxUsers);
    }

    public Channel(final String name)
    {
        this(name, 64);
    }

    public void sendMessageToChannel(final RemoteUser sender, final String type)
    {
        LOGGER.debug("Sending message to channel {} with type {}", this.getName(), type);
        final JsonObject jo = MessageBuilder.buildMessageBaseJson(type);
        final Message customMessage = new Message(jo);
        this.sendMessageToChannel(sender, customMessage);
    }

    public void sendMessageToChannel(final RemoteUser sender, final Message message)
    {
        LOGGER.debug("Sending message to channel {} with type {}", this.getName(), message.getType());
        Message msg = null;
        switch (message.getType())
        {
        case MessageType.CHANNEL_USER_CHANGE:
            msg = MessageBuilder.buildUserChangeMessage(this.userList, this);
            break;
        case MessageType.CHANNEL_MESSAGE:
            msg = MessageBuilder.buildMessagePropagateAnswer(message.getMessage(), sender.getName());
            this.addMessageToHistory(msg);
            break;
        default:
            msg = new Message("{}");
            LOGGER.error("Tried to send message to channel with unknown type: {}", msg.getType());
            break;
        }
        for (final RemoteUser user : this.userList)
        {
            user.getConnection().send(msg.toJson());
        }
        LOGGER.debug("Message sent to channel {} with {} users, Content: {}", this.name, this.userList.size(), message.toJson());
    }

    public boolean userJoin(final RemoteUser user)
    {
        LOGGER.debug("User {} wants to join Channel {}", user.getName(), this.getName());
        if (this.userList.size() < this.maxUsers)
        {
            this.userList.add(user);
            this.sendMessageToChannel(user, MessageType.CHANNEL_USER_CHANGE);
            this.sendHistoryToUser(user);
            return true;
        }
        return false;
    }

    public boolean userExit(final RemoteUser user)
    {
        LOGGER.debug("User {} wants to exit Channel {}", user.getName(), this.getName());
        final boolean success = this.userList.remove(user);
        this.sendMessageToChannel(user, MessageType.CHANNEL_USER_CHANGE);
        return success;
    }

    private void sendHistoryToUser(final RemoteUser user)
    {
        LOGGER.info("Retreive and send Channel {} Chat History to user {}", this.getName(), user.getName());
        final JsonArray chatHistory = new JsonArray();
        final List<Message> currentChannelChatHistory = this.getCurrentChannelChatHistory();
        for (final Message msg : currentChannelChatHistory)
        {
            final JsonElement jo = new JsonParser().parse(msg.toJson());
            chatHistory.add(jo);
        }
        final Message msg = MessageBuilder.buildChannelHistoryMessage(chatHistory, this);
        user.getConnection().send(msg.toJson());
        LOGGER.debug("Send Message '{}' to User '{}'", msg.toJson(), user.getName());
    }

    private List<Message> getCurrentChannelChatHistory()
    {
        LOGGER.debug("Retreiving Chat History for Channel {}", this.getName());
        final List<Message> messageHistory = new ArrayList<>();
        if (client.isStarted())
        {
            String response = "";
            final Request newRequest = client.newRequest(String.format(this.getBackendUrlTemplate(), this.getName().toLowerCase()));
            try
            {
                response = newRequest.send().getContentAsString();
                LOGGER.debug("{}", response);
                final JsonObject messageListObject = new JsonParser().parse(response).getAsJsonObject();
                if (messageListObject.get("messages") != null)
                {
                    final JsonArray messagesArray = messageListObject.get("messages").getAsJsonArray();
                    for (final JsonElement element : messagesArray)
                    {
                        messageHistory.add(new Message(element.getAsJsonObject()));
                    }
                }
            }
            catch (InterruptedException | TimeoutException | ExecutionException e)
            {
                LOGGER.error("HTTP Get not executed successfully!", e);
            }

        }
        return messageHistory;
    }

    private boolean addMessageToHistory(final Message message)
    {
        if (client.isStarted())
        {
            final Request newRequest = client.POST(String.format(this.getBackendUrlTemplate(), this.getName().toLowerCase()));
            newRequest.content(new StringContentProvider(message.toJson()), "application/json");
            try
            {
                final ContentResponse response = newRequest.send();
                return response.getStatus() == HttpStatus.OK_200;
            }
            catch (InterruptedException | TimeoutException | ExecutionException e)
            {
                LOGGER.error("HTTP Get not executed successfully!", e);
            }
        }
        return false;
    }

    public List<RemoteUser> getUserList()
    {
        return this.userList;
    }

    public int getMaxUsers()
    {
        return this.maxUsers;
    }

    public String getName()
    {
        return this.name;
    }

    public String getBackendUrlTemplate()
    {
        if (System.getenv("PORT") == null)
        {
            return HISTORY_URL_TEMPLATE_LOCAL;
        }
        return HISTORY_URL_TEMPLATE;
    }
}
