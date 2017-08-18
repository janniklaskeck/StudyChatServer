package stud.mi.server.channel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stud.mi.message.Message;
import stud.mi.server.user.RemoteUser;
import stud.mi.util.MessageBuilder;

public final class ChannelRegistry
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelRegistry.class);

    private static final Set<Channel> CHANNELS = new HashSet<>();

    private static ChannelRegistry instance;

    private ChannelRegistry()
    {
        CHANNELS.add(new Channel("Lobby"));
        CHANNELS.add(new Channel("Talk_0"));
        CHANNELS.add(new Channel("Talk_1"));
        CHANNELS.add(new Channel("Talk_2"));
        CHANNELS.add(new Channel("Support"));
        CHANNELS.add(new Channel("Other"));
    }

    public static synchronized ChannelRegistry getInstance()
    {
        if (instance == null)
        {
            instance = new ChannelRegistry();
        }
        return instance;
    }

    public void addChannel(final Channel channel)
    {
        LOGGER.debug("Adding Channel {}", channel.getName());
        CHANNELS.add(channel);
    }

    public void removeChannel(final Channel channel)
    {
        LOGGER.debug("Removing Channel {}", channel.getName());
        CHANNELS.remove(channel);
    }

    public Channel getChannel(final String channelName)
    {

        final List<Channel> channelList = CHANNELS.stream().filter(channel -> channel.getName().equalsIgnoreCase(channelName))
                .collect(Collectors.toList());
        if (channelList.isEmpty())
        {
            final Channel channel = new Channel(channelName);
            this.addChannel(channel);
            LOGGER.debug("Returning Channel with Name {}", channel.getName());
            return channel;
        }
        else
        {
            LOGGER.debug("Returning Channel with Name {}", channelList.get(0).getName());
            return channelList.get(0);
        }
    }

    public void sendChannelsToUser(final RemoteUser user)
    {
        if (user != null && user.isValid())
        {
            final Message channelsMessage = MessageBuilder.buildChannelChangeMessage(new ArrayList<>(CHANNELS));
            user.sendMessageToUser(channelsMessage.toJson());
            LOGGER.debug("Send channelList to User: {}", user.getName());
            return;
        }
        else if (user != null)
        {
            LOGGER.debug("User {} was not valid and was channel list was not sent.", user.getName());
        }
    }
}
