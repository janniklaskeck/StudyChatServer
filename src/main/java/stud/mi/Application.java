package stud.mi;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import stud.mi.server.ChatServer;

@SpringBootApplication
public class Application {

    private Application() {
    }

    public static void main(String[] args) {

        String port = System.getenv("PORT");
        final ChatServer chatServer = new ChatServer(Integer.parseInt(port));
        chatServer.start();
    }
}
