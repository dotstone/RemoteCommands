package task.client.dispatcher;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import io.vertx.core.net.NetSocket;

public class SequentialDispatcher implements TaskDispatcher {
	
	private List<NetSocket> openSockets;
	private String msg;

	public void sendMessages(Collection<NetSocket> netSockets, String msg) {
		this.openSockets = new LinkedList<>(netSockets);
		this.msg = msg;
		if(!openSockets.isEmpty()) {
			process(openSockets.get(0));
			openSockets.remove(0);
		}
	}
	
	private void process(NetSocket socket) {
		socket.write(msg);
	}

	@Override
	public String messageReceived(String result) {
		if(!openSockets.isEmpty()) {
			process(openSockets.get(0));
			openSockets.remove(0);
		}
		return result;
	}
}
