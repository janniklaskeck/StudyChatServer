package stud.mi.util;

import org.statefulj.fsm.RetryException;
import org.statefulj.fsm.model.Action;

import stud.mi.server.user.RemoteUser;

public final class ChatAction implements Action<RemoteUser>
{

    private RemoteUser user;
    private RemoteUserAction action;

    public ChatAction(final RemoteUser user, final RemoteUserAction action)
    {
        this.user = user;
        this.action = action;
    }

    @Override
    public void execute(final RemoteUser stateful, final String event, final Object... args) throws RetryException
    {
        this.action.execute(this.user, stateful, event, args);
    }
}
