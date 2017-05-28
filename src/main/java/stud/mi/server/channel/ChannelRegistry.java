package stud.mi.server.channel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        LOGGER.debug("Adding Channel {}", channel.getName());
        CHANNELS.add(channel);
    }

    public void removeChannel(final Channel channel) {
        LOGGER.debug("Removing Channel {}", channel.getName());
        CHANNELS.remove(channel);
    }

    public Channel getChannel(final String channelName) {
        LOGGER.debug("Returning Channel with Name {}", channelName);
        final List<Channel> channelList = CHANNELS.stream()
                .filter(channel -> channel.getName().equalsIgnoreCase(channelName)).collect(Collectors.toList());
        if (channelList.isEmpty()) {
            final Channel channel = new Channel(channelName);
            this.addChannel(channel);
            return channel;
        } else {
            return channelList.get(0);
        }
    }
}
