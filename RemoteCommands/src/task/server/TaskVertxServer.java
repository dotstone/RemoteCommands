package task.server;

import java.util.Scanner;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

public class TaskVertxServer {

	private static final int PORT = 4444;

	private static Vertx vertx;
	private static TaskVerticle serverVrtcl;

	public static void main(String[] args) {
		vertx = Vertx.vertx();
		
		int port = PORT;
		if(args.length == 1) {
			port = Integer.parseInt(args[0]);
		}
		System.out.println("Deploying on port " + port);
		serverVrtcl = new TaskVerticle(port);

		vertx.deployVerticle(serverVrtcl, new ServerVerticleHandler());

		System.out.println("Terminate with \"exit\"");
		
		try(Scanner scanner = new Scanner(System.in)) {
			while(!scanner.nextLine().equals("exit")) {
			}
		}

		vertx.eventBus().close(a -> {
			System.out.println("Event bus closed");
		});
		vertx.close(a -> {
			System.out.println("Vertx closed");
		});
	}

	private static class ServerVerticleHandler implements Handler<AsyncResult<String>> {

		@Override
		public void handle(AsyncResult<String> res) {
			if (res.succeeded()) {
				System.out.println("Deployed server verticle " + res.result());
			} else {
				System.out.println("Deploying server verticle failed " + res.cause().toString());
			}
		}
		
	}
}