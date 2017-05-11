package stud.mi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stud.mi.server.ChatServer;

public class ServerApp {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerApp.class);
	private static final String DEFAULT_PORT = "5000";

	private ChatServer chatServer;

	private ServerApp() {
		String port = System.getenv("PORT");
		if (port == null) {
			port = DEFAULT_PORT;
		}
		chatServer = new ChatServer(Integer.parseInt(port));
		chatServer.start();
		LOGGER.debug("Server started on port: {}", port);
	}

	public static void main(String[] args) {
		new ServerApp();
	}
}
