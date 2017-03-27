package task.client.dispatcher;

import java.util.Collection;

import io.vertx.core.net.NetSocket;

public class FastestMatchDispatcher implements TaskDispatcher {
	
	private boolean resultSent;

	public void sendMessages(Collection<NetSocket> netSockets, String msg) {
		resultSent = false;
		netSockets.forEach(socket -> socket.write(msg));
	}

	@Override
	public String messageReceived(String result) {
		if(resultSent) {
			return null;
		}
		resultSent = true;
		return result;
	}
}
