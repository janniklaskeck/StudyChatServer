package stud.mi.server;

public class Message {

    private String type;
    private Long dateSent;
    private String userName;
    private String channelName;
    private String content;

    public Message(final String type, final Long dateSent, final String userName, final String content) {
        this.type = type;
        this.dateSent = dateSent;
        this.userName = userName;
        this.content = content;
    }

    public Message() {
        // GSON Constructor
    }

    public String getType() {
        return type;
    }

    public Long getDateSent() {
        return dateSent;
    }

    public String getUserName() {
        return userName;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return String.format("Type: %s, Date: %d, Channel: %s, User: %s, Message: %s", getType(), getDateSent(),
                getChannelName(), getUserName(), getContent());
    }

}
