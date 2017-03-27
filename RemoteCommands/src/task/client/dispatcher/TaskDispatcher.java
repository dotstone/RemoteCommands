package task.client.dispatcher;

import java.util.Collection;

import io.vertx.core.net.NetSocket;

public interface TaskDispatcher {
	
	void sendMessages(Collection<NetSocket> sockets, String msg);

	String messageReceived(String msg);
}
