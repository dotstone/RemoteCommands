package task.client;

import java.util.ArrayList;
import java.util.Collection;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import task.client.dispatcher.TaskDispatcher;

public class ClientVerticle extends AbstractVerticle {

	private final TaskVertxGuiClient taskVertxGuiClient;
	
	private Collection<NetClient> netClients;
	Collection<NetSocket> netSockets;

	public ClientVerticle(TaskVertxGuiClient taskVertxGuiClient) {
		this.taskVertxGuiClient = taskVertxGuiClient;
		netClients = new ArrayList<>();
		netSockets = new ArrayList<>();
	}

	@Override
	public void start() {
		for(int port : TaskVertxGuiClient.PORTS) {
			NetClient netClient = vertx.createNetClient();

			netClient.connect(port, TaskVertxGuiClient.SERVER_ADDR, asyncResult -> {
				NetSocket netSocket = asyncResult.result();
				if(netSocket == null) {
					this.taskVertxGuiClient.log("No connection to server on port " + port);
					return;
				}
				netClients.add(netClient);
				netSockets.add(netSocket);
				this.taskVertxGuiClient.log("Connection establised on port " + port);

				netSocket.handler(inBuffer -> {
					String msg = inBuffer.getString(0, inBuffer.length());
					this.taskVertxGuiClient.log("Received result: " + msg);
					receiveMessage(msg, this.taskVertxGuiClient.selectedDispatcher);
				});

				netSocket.closeHandler(v -> {
					this.taskVertxGuiClient.log("Connection closed: " + netSocket.remoteAddress());
				});
			});	
		}
	}

	@Override
	public void stop() throws Exception {
		this.taskVertxGuiClient.log("Stopped: " + this.toString());
	}

	void sendMessage(String msg, TaskDispatcher dispatcher) {
		dispatcher.sendMessages(netSockets, msg);
	}

	private void receiveMessage(String msg, TaskDispatcher dispatcher) {
		String result = dispatcher.messageReceived(msg);
		if(result != null) {
			this.taskVertxGuiClient.messageArea.setText(result);
		}
	}
}