package stud.mi.util;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import stud.mi.message.Message;
import stud.mi.message.MessageType;
import stud.mi.server.ChatServer;
import stud.mi.server.channel.Channel;
import stud.mi.server.user.RemoteUser;

public class MessageBuilder {

    private static final String CHANNEL_NAME = "channelName";
    private static final String USER_ID = "userID";
    private static final String SUCCESS = "success";
    private static final String VERSION = "version";
    private static final String TYPE = "type";
    private static final String CONTENT = "content";
    private static final String MESSAGE = "message";
    private static final String USER_NAME = "userName";
    private static final String CHANNEL_USER_NAMES = "channelUserNames";

    private MessageBuilder() {
    }

    public static Message buildUserJoinMessage(final List<RemoteUser> users, final Channel channel) {
        final JsonObject msgBase = buildMessageBaseJson(MessageType.CHANNEL_USER_JOIN);
        final Message msg = new Message(msgBase);
        final JsonArray userNameArray = new JsonArray();
        for (final RemoteUser user : users) {
            userNameArray.add(user.getName());
        }
        msg.getContent().add(CHANNEL_USER_NAMES, userNameArray);
        msg.getContent().addProperty(CHANNEL_NAME, channel.getName());
        return msg;
    }

    public static Message buildAckUserJoinChannel(final Long userID, final Channel channel) {
        final JsonObject msgBase = buildMessageBaseJson(MessageType.ACK_CHANNEL_JOIN);
        final Message msg = new Message(msgBase);
        msg.getContent().addProperty(USER_ID, userID);
        msg.getContent().addProperty(CHANNEL_NAME, channel.getName());
        return msg;
    }

    public static Message buildSendUserID(final Long userID) {
        final JsonObject msgBase = buildMessageBaseJson(MessageType.USER_JOIN);
        final Message msg = new Message(msgBase);
        msg.getContent().addProperty(USER_ID, userID);
        return msg;
    }

    public static Message buildMessagePropagateAnswer(final String message, final String senderName) {
        final JsonObject msgBase = buildMessageBaseJson(MessageType.CHANNEL_MESSAGE);
        final Message msg = new Message(msgBase);
        msg.getContent().addProperty(MESSAGE, message);
        msg.getContent().addProperty(USER_NAME, senderName);
        return msg;
    }

    public static Message buildChannelJoinAnswer(final String channelName, final boolean success) {
        final JsonObject msgBase = buildMessageBaseJson(MessageType.CHANNEL_JOIN);
        final Message msg = new Message(msgBase);
        msg.getContent().addProperty(CHANNEL_NAME, channelName);
        msg.getContent().addProperty(SUCCESS, success);
        return msg;
    }

    public static Message buildChannelExitAnswer(final String channelName, final boolean success) {
        final JsonObject msgBase = buildMessageBaseJson(MessageType.CHANNEL_EXIT);
        final Message msg = new Message(msgBase);
        msg.getContent().addProperty(CHANNEL_NAME, channelName);
        msg.getContent().addProperty(SUCCESS, success);
        return msg;
    }

    public static Message buildSendMessageAnswer(final String channelName, final boolean success) {
        final JsonObject msgBase = buildMessageBaseJson(MessageType.CHANNEL_MESSAGE_REPLY);
        final Message msg = new Message(msgBase);
        msg.getContent().addProperty(CHANNEL_NAME, channelName);
        msg.getContent().addProperty(SUCCESS, success);
        return msg;
    }

    public static JsonObject buildMessageBaseJson(final String type) {
        final JsonObject jo = new JsonObject();
        jo.addProperty(VERSION, ChatServer.PROTOCOL_VERSION);
        jo.addProperty(TYPE, type);
        jo.add(CONTENT, new JsonObject());
        return jo;
    }
}
