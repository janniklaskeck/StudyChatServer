package stud.mi.server.user;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.fsm.FSM;
import org.statefulj.fsm.TooBusyException;
import org.statefulj.fsm.model.State;
import org.statefulj.fsm.model.impl.StateImpl;
import org.statefulj.persistence.memory.MemoryPersisterImpl;

import stud.mi.server.channel.Channel;
import stud.mi.util.ChatAction;
import stud.mi.util.MessageBuilder;

public final class ClientConnectionStateMachine extends FSM<RemoteUser>
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConnectionStateMachine.class);

    private RemoteUser remoteUser;
    private List<State<RemoteUser>> states = new ArrayList<>();

    public ClientConnectionStateMachine(final RemoteUser user)
    {
        super("ClientConnectionFSM");
        this.remoteUser = user;

        this.createStateMachine();
    }

    private void createStateMachine()
    {
        final StateImpl<RemoteUser> connectingState = new StateImpl<>(UserStates.CONNECTING.getValue());
        final StateImpl<RemoteUser> connectedState = new StateImpl<>(UserStates.CONNECTED.getValue());
        final StateImpl<RemoteUser> connectedChannelState = new StateImpl<>(UserStates.CONNECTED_CHANNEL.getValue());
        final StateImpl<RemoteUser> disconnectedState = new StateImpl<>(UserStates.DISCONNECTED.getValue(), true);

        final ChatAction onRegister = new ChatAction(this.remoteUser, (user, state, event, args) ->
        {
            final String message = MessageBuilder.buildSendUserID(user.getID()).toJson();
            LOGGER.debug("Send Message: {}", message);
            user.sendMessageToUser(message);
        });

        final ChatAction onJoinChannel = new ChatAction(this.remoteUser, (user, state, event, args) ->
        {
            final Channel channelToJoin = (Channel) args[0];
            user.joinChannel(channelToJoin);
            final String message = MessageBuilder.buildAckUserJoinChannel(user.getID(), channelToJoin).toJson();
            LOGGER.debug("Send Message: {}", message);
            user.sendMessageToUser(message);
        });
        final ChatAction onDisconnectServer = new ChatAction(this.remoteUser, (user, state, event, args) ->
        {
            user.exitChannel();
            UserRegistry.getInstance().removeUser(user.getID());
        });

        final ChatAction onDisconnectChannel = new ChatAction(this.remoteUser, (user, state, event, args) -> user.exitChannel());

        connectingState.addTransition(UserEvents.ACK_REGISTER.getValue(), connectedState, onRegister);
        connectedState.addTransition(UserEvents.JOIN_CHANNEL.getValue(), connectedChannelState, onJoinChannel);
        connectedState.addTransition(UserEvents.DISCONNECT_SERVER.getValue(), disconnectedState, onDisconnectServer);
        connectedChannelState.addTransition(UserEvents.JOIN_CHANNEL.getValue(), connectedChannelState, onJoinChannel);
        connectedChannelState.addTransition(UserEvents.DISCONNECT_CHANNEL.getValue(), connectedState, onDisconnectChannel);
        connectedChannelState.addTransition(UserEvents.DISCONNECT_SERVER.getValue(), disconnectedState, onDisconnectServer);

        this.states.add(connectingState);
        this.states.add(connectedState);
        this.states.add(connectedChannelState);
        this.states.add(disconnectedState);
        final MemoryPersisterImpl<RemoteUser> persister = new MemoryPersisterImpl<>(this.states, connectingState);
        this.setPersister(persister);

    }

    public State<RemoteUser> processEvent(final UserEvents event)
    {
        try
        {
            return super.onEvent(this.remoteUser, event.getValue(), (Object[]) null);
        }
        catch (final TooBusyException e)
        {
            LOGGER.debug("StateMachine too busy.", e);
        }
        return null;
    }

    public State<RemoteUser> processEvent(final UserEvents event, final Object... args)
    {
        try
        {
            return super.onEvent(this.remoteUser, event.getValue(), args);
        }
        catch (final TooBusyException e)
        {
            LOGGER.debug("StateMachine too busy.", e);
        }
        return null;
    }

}
