package stud.mi.server.user;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.persistence.annotations.State;

import stud.mi.server.channel.Channel;

public class RemoteUser
{

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteUser.class);
    private static final long MAX_ALIVE_SECONDS = 20L;

    @State
    private String state;

    private String name;
    private Long userID;
    private Channel jointChannel;
    private WebSocket connection;
    private ClientConnectionStateMachine stateMachine;
    private LocalDateTime lastHeartBeat = LocalDateTime.now();

    public RemoteUser(final WebSocket connection, final String name, final Long designatedID)
    {
        RemoteUser.LOGGER.debug("Create User {} with ID {}", name, designatedID);
        this.name = name;
        this.userID = designatedID;
        this.connection = connection;
        this.stateMachine = new ClientConnectionStateMachine(this);
    }

    public void destroy()
    {
        RemoteUser.LOGGER.debug("Destroy user {}", this.getName());
        this.exitChannel();
    }

    public boolean exitChannel()
    {
        if (this.jointChannel != null)
        {
            RemoteUser.LOGGER.debug("User {} exits channel {}", this.getName(), this.jointChannel.getName());
            return this.jointChannel.userExit(this);
        }
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

    public void heartBeat()
    {
        RemoteUser.LOGGER.trace("Hearbeat on User {}", this.getName());
        this.lastHeartBeat = LocalDateTime.now();
    }

    public boolean isDead()
    {
        final long seconds = ChronoUnit.SECONDS.between(this.lastHeartBeat, LocalDateTime.now());
        return seconds > RemoteUser.MAX_ALIVE_SECONDS;
    }

    public boolean isValid()
    {
        return !this.name.isEmpty() && this.userID > 0;
    }

    public boolean joinChannel(final Channel channel)
    {
        RemoteUser.LOGGER.debug("User {} joins channel {}", this.getName(), channel.getName());
        this.exitChannel();
        if (channel.userJoin(this))
        {
            this.jointChannel = channel;
            return true;
        }
        return false;
    }

}
