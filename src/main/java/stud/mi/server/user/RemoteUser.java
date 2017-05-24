package stud.mi.server.user;

import org.java_websocket.WebSocket;

import stud.mi.server.channel.Channel;

public class RemoteUser {

	private String name;
	private Long userID;
	private Channel jointChannel;
	private WebSocket connection;

	public RemoteUser(final WebSocket connection, final String name, final Long designatedID) {
		this.name = name;
		this.userID = designatedID;
		this.connection = connection;
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
}
