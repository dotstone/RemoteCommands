package task.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import task.common.GenericSourceTask;
import task.common.Task;

import java.util.ArrayList;
import java.util.Collection;

public class SourceCodeExecutorVerticle extends AbstractVerticle {
	
	private static final String EXECUTOR_NAME = "executor";
	
	private NetServer netServer;
	private Collection<NetSocket> clients;
	private EventBus eventBus;
	
	@Override
	public void start() throws Exception {
		clients = new ArrayList<>();
		
		eventBus = vertx.eventBus(); 
		
		netServer = vertx.createNetServer();
		netServer.connectHandler(netSocket -> {
			clients.add(netSocket);
			
			netSocket.handler(inBuffer -> {
				String work = inBuffer.getString(0, inBuffer.length()); 
				Task<?> task = GenericSourceTask.fromMessage(work); 
				eventBus.send(EXECUTOR_NAME, task.toString(), (AsyncResult<Message<String>> asynReply) -> {
					System.out.format("Scheduled task reply: %s%n", asynReply.result());
				}); 
			});
			
			netSocket.closeHandler(v -> {
				clients.remove(netSocket);
				System.out.println("Connection closed " + netSocket.remoteAddress());
			});
		});
	}

	@Override
	public void stop() throws Exception {
		clients.forEach(c -> c.close());
	}
}
