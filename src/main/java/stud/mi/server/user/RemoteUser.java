package stud.mi.server.user;

import org.java_websocket.WebSocket;
import org.statefulj.persistence.annotations.State;

import stud.mi.server.channel.Channel;

public class RemoteUser {

    @State
    private String state;

    private String name;
    private Long userID;
    private Channel jointChannel;
    private WebSocket connection;
    private ClientConnectionStateMachine stateMachine;

    public RemoteUser(final WebSocket connection, final String name, final Long designatedID) {
        this.name = name;
        this.userID = designatedID;
        this.connection = connection;
        this.stateMachine = new ClientConnectionStateMachine(this);
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

    public boolean joinChannel(final Channel channel) {
        this.exitChannel();
        if (channel.userJoin(this)) {
            this.jointChannel = channel;
            return true;
        }
        return false;
    }

    public boolean exitChannel() {
        if (this.jointChannel != null) {
            return this.jointChannel.userExit(this);
        }
        return false;
    }

    public Channel getJointChannel() {
        return jointChannel;
    }

    public void destroy() {
        this.exitChannel();

    }

}
