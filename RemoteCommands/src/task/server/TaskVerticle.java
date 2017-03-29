package task.server;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import task.common.GenericSourceTask;
import task.common.Task;

public class TaskVerticle extends AbstractVerticle {
	
	private final int port;
	
	private NetServer netServer;
	private List<NetSocket> clients;

	public TaskVerticle(int port) {
		this.port = port;
	}
	@Override
	public void start() throws Exception {
		clients = new ArrayList<>(); 
		
		netServer = vertx.createNetServer();
		netServer.connectHandler(netSocket -> {
			System.out.println("Connected with: " + netSocket.remoteAddress());
			clients.add(netSocket);
			
			netSocket.handler(inBuffer -> {
				String work = inBuffer.getString(0, inBuffer.length());
				final Task<Object> task = GenericSourceTask.fromMessage(work);
				TaskResult<Object> result = task.get();
				if(result.isValid()) {
					netSocket.write(result.getResult().toString());
				} else {
					netSocket.write(result.getError());
				} 
			});
			
			netSocket.closeHandler(v -> {
				clients.remove(netSocket);
				System.out.println("Disconnected from: " + netSocket.remoteAddress());
			});
		});
		try {
			netServer.listen(port);
		} catch(Exception e) {
			System.out.println("Failed to start server!");
			e.printStackTrace();
		}
	}

	@Override
	public void stop() throws Exception {
		clients.forEach(NetSocket::close);		
	}
}