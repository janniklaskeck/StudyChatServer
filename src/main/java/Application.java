
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.Collection;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class Application {

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        String port = System.getenv("PORT");
        System.out.println(port);
        WebSocketServer wss = new WebSocketServer(new InetSocketAddress(Integer.parseInt(port))) {

            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                System.out.println("Server open");
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                System.out.println("Server message " + message);

            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                ex.printStackTrace();

            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                System.out.println("Closing");
            }
        };

        wss.start();

        BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String in = sysin.readLine();
            sendToAll(wss, in);
            if (in.equals("exit")) {
                wss.stop();
                break;
            }
        }
    }

    public static void sendToAll(WebSocketServer wss, String text) {
        Collection<WebSocket> con = wss.connections();
        synchronized (con) {
            for (WebSocket c : con) {
                c.send(text);
            }
        }
    }

}
