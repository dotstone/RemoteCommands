package task.client;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

public class TaskVertxGuiClient {
	
	private static TaskVertxGuiClient clientApp; 
	private static final String SERVER_ADDR = "localhost";
	private static final int PORT = 4444;

	public static void main(String[] args) {
		clientApp = new TaskVertxGuiClient(); 
		clientApp.start();  
	}
	
	private JFrame frame; 
	private JTextField inputField; 
	private JTextArea messageArea; 
	private JTextArea logArea; 
	private Vertx vertx;
	private MsgClientVerticle clientVrtl;
	
	private void start() {
	
		frame = new JFrame("Task client"); 
		
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				log("Exiting");
				clientVrtl.netSocket.close();
				vertx.close();
				frame.dispose();
			}
			
		});
		JPanel contentPane = new JPanel(new BorderLayout()); 
		frame.setContentPane(contentPane);
		
		JPanel inputPanel = new JPanel(); 
		inputPanel.setBorder(BorderFactory.createTitledBorder("Input:"));
		contentPane.add(inputPanel, BorderLayout.NORTH);
		
		inputField = new JTextField(40);
		inputPanel.add(inputField); 
		inputField.addActionListener(a -> {
			String msg = inputField.getText(); 
			log("Input from user: " + msg);
			clientVrtl.sendMessage(msg);
		});
		
		JPanel outputPanel = new JPanel(); 
		outputPanel.setBorder(BorderFactory.createTitledBorder("Results:"));
		contentPane.add(outputPanel, BorderLayout.CENTER);
		
		messageArea = new JTextArea(20, 40); 
		outputPanel.add(new JScrollPane(messageArea));
		
		JPanel logPanel = new JPanel(); 
		logPanel.setBorder(BorderFactory.createTitledBorder("Log:"));
		contentPane.add(logPanel, BorderLayout.SOUTH);
		
		logArea = new JTextArea(10, 40); 
		logPanel.add(new JScrollPane(logArea));
		
		frame.pack(); 
		frame.setLocation(200,  200);
		frame.setVisible(true); 
		
		vertx = Vertx.vertx(); 
		
		clientVrtl = new MsgClientVerticle();
		vertx.deployVerticle(clientVrtl, asyncResult -> {
			if (asyncResult.succeeded()) {
				log("Deployed client: " + asyncResult.result());
			} else {
				log("Deploying client failed: " + asyncResult.cause().toString());
			}
		});
		log("Called deploy ");

		
	}
	
	private void log(String logMsg) {
		logArea.append(logMsg + "\n");
	}
	
	private class MsgClientVerticle extends AbstractVerticle {

		private NetClient netClient;
		private NetSocket netSocket;

		public MsgClientVerticle() {
		}

		@Override
		public void start() throws Exception {

			netClient = vertx.createNetClient();

			netClient.connect(PORT, SERVER_ADDR, asyncResult -> {
				netSocket = asyncResult.result();
				log("Connection establised: " + netSocket.remoteAddress());

				netSocket.handler(inBuffer -> {
					String msg = inBuffer.getString(0, inBuffer.length());
					log("Received message: " + msg);
					displayMessage(msg);
				});

				netSocket.closeHandler(v -> {
					log("Connection closed: " + netSocket.remoteAddress());
				});
				
			});

		}

		@Override
		public void stop() throws Exception {
			log("Stopped: " + this.toString());
		}

		void sendMessage(String msg) {
			log("Sending message: " + msg);
			netSocket.write(msg); 		
		}

		private void displayMessage(String msg) {
			messageArea.append(String.format("Result: %s%n", msg));
		}
		
	}

}
