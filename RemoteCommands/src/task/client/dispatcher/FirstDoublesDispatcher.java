package task.client.dispatcher;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import io.vertx.core.net.NetSocket;

public class FirstDoublesDispatcher implements TaskDispatcher {
	
	private boolean sentResult;
	
	private Set<String> results;

	public void sendMessages(Collection<NetSocket> netSockets, String msg) {
		results = new HashSet<>();
		sentResult = false;
		netSockets.forEach(socket -> socket.write(msg));
	}

	@Override
	public String messageReceived(String msg) {
		if(msg == null) {
			return null;
		}
		if(sentResult) {
			return null;
		}
		if(results.contains(msg)) {
			sentResult = true;
			return msg;
		}
		results.add(msg);
		return null;
	}
}
