package task.server;

import java.util.Scanner;

import io.vertx.core.Vertx;

public class TaskVertxServer {

	static final int PORT = 4444;

	static Vertx vertx;
	static TaskMasterVerticle serverVrtcl;

	public static void main(String[] args) {

		vertx = Vertx.vertx();
		serverVrtcl = new TaskMasterVerticle();

		vertx.deployVerticle(serverVrtcl, res -> {
			if (res.succeeded()) {
				System.out.println("Deployed server verticle " + res.result());
			} else {
				System.out.println("Deploying server verticle failed " + res.cause().toString());
			}
		});
		System.out.println("Called deployVerticle ");

		System.out.println("Terminate with exit");
		
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

}