package stud.mi.server.user;

public enum UserEvents {
    ACK_REGISTER("ACK_REGISTER"), JOIN_CHANNEL("JOIN_CHANNEL"), DISCONNECT_SERVER(
            "DISCONNECT_SERVER"), DISCONNECT_CHANNEL("DISCONNECT_CHANNEL");

    private String value;

    private UserEvents(final String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
