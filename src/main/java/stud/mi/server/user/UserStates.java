package stud.mi.server.user;

public enum UserStates {
    CONNECTING("CONNECTING"), CONNECTED("CONNECTED"), DISCONNECTED("DISCONNECT"), CONNECTED_CHANNEL(
            "CONNECTED_CHANNEL");

    private String value;

    private UserStates(final String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }

}
