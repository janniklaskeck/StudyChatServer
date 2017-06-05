package stud.mi.server.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stud.mi.message.Message;

public class UserRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRegistry.class);

    private static final Map<Long, RemoteUser> USERS = new HashMap<>();
    private static final long HEARTBEAT_RATE = 15 * 1000L;

    private static UserRegistry instance;

    private Timer heartBeatTimer;

    private UserRegistry() {
        this.heartBeatTimer = new Timer(true);
        heartBeatTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                LOGGER.trace("Running Heartbeat.");
                final List<Long> deadUserIds = new ArrayList<>();
                for (Map.Entry<Long, RemoteUser> user : USERS.entrySet()) {
                    if (user.getValue().isDead()) {
                        LOGGER.debug("User {} is dead", user.getValue().getName());
                        deadUserIds.add(user.getKey());
                    }
                }
                deadUserIds.forEach(instance::removeUser);
            }
        }, 1000L, HEARTBEAT_RATE);
    }

    public static synchronized UserRegistry getInstance() {
        if (instance == null) {
            instance = new UserRegistry();
        }
        return instance;
    }

    public RemoteUser getUser(final Long userID) {
        return USERS.get(userID);
    }

    public void removeUser(final Long userID) {
        LOGGER.debug("Removing User with ID {}", userID);
        USERS.get(userID).destroy();
        USERS.remove(userID);
    }

    public void removeUser(final WebSocket connection) {
        this.removeUser(getUser(connection).getID());
    }

    private RemoteUser getUser(final WebSocket connection) {
        return USERS.entrySet().stream().filter(entry -> entry.getValue().getConnection().equals(connection))
                .collect(Collectors.toList()).get(0).getValue();
    }

    public void registerUser(final WebSocket connection, final Message registerMessage) {
        LOGGER.debug("Regiser User {}", registerMessage.getUserName());
        final boolean userAlreadyExists = !USERS.entrySet().stream()
                .filter(entry -> entry.getValue().getName().equalsIgnoreCase(registerMessage.getUserName()))
                .collect(Collectors.toList()).isEmpty();
        final Long newUserID;
        if (userAlreadyExists) {
            newUserID = -1L;
            LOGGER.debug("User {} already registered.", registerMessage.getUserName());
        } else {
            newUserID = generateUserID();
        }
        final RemoteUser user = new RemoteUser(connection, registerMessage.getUserName(), newUserID);
        user.getStateMachine().processEvent(UserEvents.ACK_REGISTER);
        if (user.getID() > 0) {
            USERS.put(user.getID(), user);
        }
        LOGGER.debug("Registered new User with Name {} and ID {}", user.getName(), user.getID());
    }

    private Long generateUserID() {
        final Random rnd = new Random();
        long userID = rnd.nextLong();
        while (USERS.containsKey(userID) || userID <= 0L) {
            userID = rnd.nextLong();
        }
        LOGGER.debug("Return new User ID {}.", userID);
        return userID;
    }

}
