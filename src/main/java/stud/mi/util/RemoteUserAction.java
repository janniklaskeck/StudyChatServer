package stud.mi.util;

import stud.mi.server.user.RemoteUser;

@FunctionalInterface
public interface RemoteUserAction {

    void execute(final RemoteUser user, final RemoteUser stateful, final String event, final Object... args);

}
