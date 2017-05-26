package stud.mi.message;

public class MessageType {

    public static final String USER_JOIN = "USER_JOIN";
    public static final String CHANNEL_MESSAGE = "CHANNEL_MSG";
    public static final String CHANNEL_MESSAGE_REPLY = "CHANNEL_MSG_REPLY";
    public static final String CHANNEL_JOIN = "CHANNEL_JOIN";
    public static final String ACK_CHANNEL_JOIN = "ACK_CHANNEL_JOIN";
    public static final String CHANNEL_EXIT = "CHANNEL_EXIT";

    private MessageType() {
    }

}
