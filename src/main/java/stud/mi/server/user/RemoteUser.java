package stud.mi.server.user;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.persistence.annotations.State;

import stud.mi.server.channel.Channel;

public final class RemoteUser
{

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteUser.class);
    static final long MAX_ALIVE_SECONDS = 20L;

    @State
    private String state;

    private String name;
    private Long userID;
    private Channel jointChannel;
    private WebSocket connection;
    private ClientConnectionStateMachine stateMachine;
    LocalDateTime lastHeartBeat = LocalDateTime.now();

    public RemoteUser(final WebSocket connection, final String name, final Long designatedID)
    {
        LOGGER.debug("Create User {} with ID {}", name, designatedID);
        this.name = name;
        this.userID = designatedID;
        this.connection = connection;
        this.stateMachine = new ClientConnectionStateMachine(this);
    }

    public void heartBeat()
    {
        LOGGER.trace("Hearbeat on User {}", this.getName());
        this.lastHeartBeat = LocalDateTime.now();
    }

    public boolean isDead()
    {
        final long seconds = ChronoUnit.SECONDS.between(this.lastHeartBeat, LocalDateTime.now());
        LOGGER.debug("Seconds between heartbeats: {}", seconds);
        return seconds > RemoteUser.MAX_ALIVE_SECONDS;
    }

    public boolean isValid()
    {
        return !this.name.isEmpty() && this.userID > 0;
    }

    public boolean joinChannel(final Channel channel)
    {
        LOGGER.debug("User {} joins channel {}", this.getName(), channel.getName());
        this.exitChannel();
        if (channel.userJoin(this))
        {
            this.jointChannel = channel;
            return true;
        }
        return false;
    }

    public void destroy()
    {
        LOGGER.debug("Destroy user {}", this.getName());
        this.exitChannel();
    }

    public boolean exitChannel()
    {
        if (this.jointChannel != null)
        {
            LOGGER.debug("User {} exits channel {}", this.getName(), this.jointChannel.getName());
            if (this.jointChannel.userExit(this))
            {
                this.jointChannel = null;
                return true;
            }
        }
        return false;
    }

    public boolean sendMessageToUser(final String msg)
    {
        LOGGER.trace("Trying to send Message '{}' to User '{}'.", msg, this.getName());
        if (this.connection != null)
        {
            this.connection.send(msg);
            return true;
        }
        LOGGER.error("Connection not existing, Message not send!");
        return false;
    }

    public WebSocket getConnection()
    {
        return this.connection;
    }

    public Long getID()
    {
        return this.userID;
    }

    public Channel getJointChannel()
    {
        return this.jointChannel;
    }

    public String getName()
    {
        return this.name;
    }

    public ClientConnectionStateMachine getStateMachine()
    {
        return this.stateMachine;
    }

}
