package stud.mi.server.user;

import org.statefulj.persistence.annotations.State;

public class RemoteUserState {

	@State
	private String state;

	public String getState() {
		return this.state;
	}

}
