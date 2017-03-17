package task.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

import tasks.common.Task;

class TaskSlaveVerticle extends AbstractVerticle {

	final String name; 
	EventBus eventBus; 

	TaskSlaveVerticle(String name) {
		this.name = name; 
	}
	
	@Override
	public void start() throws Exception {
		eventBus = vertx.eventBus(); 
		
		MessageConsumer<String> taskReceiver = eventBus.consumer(name);
		taskReceiver.handler(message -> {
			String s = message.body(); 
			Task task = Task.fromString(s);
			System.out.println(name + ": Task received " + task.toString());
			message.reply(name + ":OK");
			CompletableFuture<String> ftrResult = computeResultsAsync(task); 
			ftrResult.thenAcceptAsync(result -> {
				task.result = result; 
				eventBus.send("master", task.toString()); 
			});
		}); 
		
	}

	private CompletableFuture<String> computeResultsAsync(Task task) {
		
		return CompletableFuture.supplyAsync(() -> {
			block(500);     // simulates long computation time 
			return "Done";  // should return real result 
		}); 
	}

	@Override
	public void stop() throws Exception {
	}

	/**
	 * Simulates a long lasting blocking operation
	 */
	static final Random RAND = new Random(); 
	private void block(long avgMs) {  
		try {
			Thread.sleep(avgMs + RAND.nextInt((int)(avgMs)));
		} catch (InterruptedException e) { }
	}
}