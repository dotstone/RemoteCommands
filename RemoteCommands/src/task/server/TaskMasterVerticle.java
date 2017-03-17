package task.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tasks.common.Task;

public class TaskMasterVerticle extends AbstractVerticle {
	
	private static int N_SLAVES = 4; 
	
	private NetServer netServer;
	private List<NetSocket> clients;
	private EventBus eventBus; 
	private TaskSlaveVerticle[] slaves; 
	private int iSlave = 0; 
	private Map<Task, NetSocket> tasks; 
	private int taskNr = 0; 

	@Override
	public void start() throws Exception {
		
		clients = new ArrayList<>(); 
		tasks = new HashMap<Task, NetSocket>(); 
		eventBus = vertx.eventBus(); 
		
		slaves = new TaskSlaveVerticle[N_SLAVES]; 
		for (int i = 0; i < N_SLAVES; i++) {
			TaskSlaveVerticle slave = new TaskSlaveVerticle("slave" + i);
			vertx.deployVerticle(slave);
			slaves[i] = slave; 
		}
		
		netServer = vertx.createNetServer();
		netServer.connectHandler(netSocket -> {
			System.out.println("Master: Connection opened from " + netSocket.remoteAddress());
			clients.add(netSocket);
			
			netSocket.handler(inBuffer -> {
				String work = inBuffer.getString(0, inBuffer.length());
				System.out.println("Master: Work received " + work);
				TaskSlaveVerticle slave = slaves[iSlave]; 
				iSlave = (iSlave + 1) % N_SLAVES; 
				final Task task = new Task(taskNr++, slave.name, work); 
				tasks.put(task, netSocket);  
				eventBus.send(slave.name, task.toString(), (AsyncResult<Message<String>> asynReply) -> {
					System.out.format("Master: Slave %s replied with %s%n", slave, asynReply.result());
				}); 
			});
			
			netSocket.closeHandler(v -> {
				clients.remove(netSocket);
				System.out.println("Connection closed " + netSocket.remoteAddress());
			});
		});
		
		eventBus.consumer("master", (Message<String> message) -> {
			String result = message.body(); 
			Task task = Task.fromString(result); 
			System.out.format("Master: Slave %s done with %s%n", task.slave, task.result);
			NetSocket client = tasks.get(task); 
			client.write(task.result); 
			tasks.remove(task);
		}); 

		netServer.listen(TaskVertxServer.PORT);
	}

	@Override
	public void stop() throws Exception {
		for (NetSocket client : clients) {
			client.close();
		}
		for (TaskSlaveVerticle slave : slaves) {
			vertx.undeploy(slave.deploymentID());
		}
		
	}
	
}