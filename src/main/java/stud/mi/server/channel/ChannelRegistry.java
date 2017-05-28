package stud.mi.server.channel;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelRegistry.class);

    private static final Set<Channel> CHANNELS = new HashSet<>();

    private static ChannelRegistry instance;

    private ChannelRegistry() {
    }

    public static ChannelRegistry getInstance() {
        if (instance == null) {
            instance = new ChannelRegistry();
        }
        return instance;
    }

    public void addChannel(final Channel channel) {
        CHANNELS.add(channel);
    }

    public void removeChannel(final Channel channel) {
        CHANNELS.remove(channel);
    }
}
