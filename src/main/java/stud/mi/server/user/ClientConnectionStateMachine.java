package stud.mi.server.user;

import java.util.ArrayList;
import java.util.List;

import org.statefulj.fsm.FSM;
import org.statefulj.fsm.model.State;
import org.statefulj.fsm.model.impl.StateImpl;
import org.statefulj.persistence.memory.MemoryPersisterImpl;

enum UserEvents {
	JOIN("JOIN"), LEAVE("LEAVE"), SEND_TO("SEND_TO"), SEND_FROM("SEND_FROM"), JOIN_CHANNEL("JOIN_CHANNEL");

	private String value;

	private UserEvents(final String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}

enum UserStates {
	CONNECTED_SERVER("CONNECTED_SERVER"), LEFT_SERVER("LEFT_SERVER"), CONNECTED_CHANNEL("CONNECTED_CHANNEL");

	private String value;

	private UserStates(final String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}

public class ClientConnectionStateMachine {

	private FSM<Object> stateMachine;
	private RemoteUser user;
	private List<State<RemoteUserState>> states = new ArrayList<>();

	public ClientConnectionStateMachine(final RemoteUser user) {
		this.user = user;
		createStates();
		createStateMachine();
	}

	private void createStates() {
		states.add(new StateImpl<>(""));
	}

	private void createStateMachine() {
		stateMachine = new FSM<>(new MemoryPersisterImpl<>());
	}

	public void update() {

	}

}
