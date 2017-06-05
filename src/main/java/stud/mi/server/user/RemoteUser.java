package stud.mi.server.user;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.persistence.annotations.State;

import stud.mi.server.channel.Channel;

public class RemoteUser {

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

    public RemoteUser(final WebSocket connection, final String name, final Long designatedID) {
        LOGGER.debug("Create User {} with ID {}", name, designatedID);
        this.name = name;
        this.userID = designatedID;
        this.connection = connection;
        this.stateMachine = new ClientConnectionStateMachine(this);
    }

    public void heartBeat() {
        LOGGER.debug("Hearbeat on User {}", getName());
        lastHeartBeat = LocalDateTime.now();
    }

    public boolean isDead() {
        final long seconds = ChronoUnit.SECONDS.between(lastHeartBeat, LocalDateTime.now());
        return seconds > MAX_ALIVE_SECONDS;
    }

    public boolean joinChannel(final Channel channel) {
        LOGGER.debug("User {} joins channel {}", getName(), channel.getName());
        this.exitChannel();
        if (channel.userJoin(this)) {
            this.jointChannel = channel;
            return true;
        }
        return false;
    }

    public boolean exitChannel() {
        if (this.jointChannel != null) {
            LOGGER.debug("User {} exits channel {}", getName(), jointChannel.getName());
            return this.jointChannel.userExit(this);
        }
        return false;
    }

    public Channel getJointChannel() {
        return jointChannel;
    }

    public ClientConnectionStateMachine getStateMachine() {
        return this.stateMachine;
    }

    public WebSocket getConnection() {
        return this.connection;
    }

    public String getName() {
        return this.name;
    }

    public Long getID() {
        return userID;
    }

    public void destroy() {
        LOGGER.debug("Destroy user {}", getName());
        this.exitChannel();
    }

}
