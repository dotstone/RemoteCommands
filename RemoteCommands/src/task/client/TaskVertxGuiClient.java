package task.client;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;

import com.google.common.collect.Lists;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetSocket;
import task.client.dispatcher.FastestMatchDispatcher;
import task.client.dispatcher.FirstDoublesDispatcher;
import task.client.dispatcher.MostMatchesDispatcher;
import task.client.dispatcher.SequentialDispatcher;
import task.client.dispatcher.TaskDispatcher;
import task.common.GenericSourceTask;

public class TaskVertxGuiClient {
	
	static final String SERVER_ADDR = "localhost";
	
	static final Collection<Integer> PORTS = Lists.newArrayList(4444, 4445, 4446, 4447, 4448);
	
	private static final String DEFAULT_CODE = "import java.util.function.Supplier;" + System.lineSeparator() + System.lineSeparator() +
			"public class MyTask implements Supplier<String> {" + System.lineSeparator() +
			"    public String get() {" + System.lineSeparator() + 
			"        return \"Hello World!\";" + System.lineSeparator() +
  			"    }" + System.lineSeparator() +
			"}";

	public static void main(String[] args) {
		new TaskVertxGuiClient().start();
	}
	
	private JFrame frame; 
	private JTextArea inputField; 
	JTextField messageArea; 
	private JTextArea logArea; 
	private Vertx vertx;
	private ClientVerticle clientVrtl;
	
	private JRadioButton radioMostMatches, radioFastestMatch, radioDoublesMatch, radioSequential;
	TaskDispatcher selectedDispatcher;

	private final FastestMatchDispatcher fastestMatch = new FastestMatchDispatcher();
	private final MostMatchesDispatcher mostMatches = new MostMatchesDispatcher();
	private final FirstDoublesDispatcher firstDoubles = new FirstDoublesDispatcher();
	private final SequentialDispatcher sequential = new SequentialDispatcher();
	
	private void start() {
		frame = new JFrame("Task client"); 
		
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				log("Exiting");
				for(NetSocket socket : clientVrtl.netSockets) {
					socket.close();
				}
				vertx.close();
				frame.dispose();
			}
			
		});
		
		JPanel contentPane = new JPanel(new BorderLayout()); 
		frame.setContentPane(contentPane);
	
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
		inputPanel.setBorder(BorderFactory.createTitledBorder("Input:"));
		contentPane.add(inputPanel, BorderLayout.NORTH);
		
		JPanel radioPanel = new JPanel();
		radioMostMatches = new JRadioButton("Most Matches");
		radioFastestMatch = new JRadioButton("Fastest Match");
		radioDoublesMatch = new JRadioButton("First Double");
		radioSequential = new JRadioButton("Sequential");
		radioSequential.setSelected(true);
		selectedDispatcher = sequential;
		radioMostMatches.addActionListener(a -> {
			radioFastestMatch.setSelected(false);
			radioDoublesMatch.setSelected(false);
			radioSequential.setSelected(false);
			selectedDispatcher = mostMatches;
		});
		radioFastestMatch.addActionListener(a -> {
			radioMostMatches.setSelected(false);
			radioDoublesMatch.setSelected(false);
			radioSequential.setSelected(false);
			selectedDispatcher = fastestMatch;
		});
		radioDoublesMatch.addActionListener(a -> {
			radioFastestMatch.setSelected(false);
			radioMostMatches.setSelected(false);
			radioSequential.setSelected(false);
			selectedDispatcher = firstDoubles;
		});
		radioSequential.addActionListener(a -> {
			radioFastestMatch.setSelected(false);
			radioMostMatches.setSelected(false);
			radioDoublesMatch.setSelected(false);
			selectedDispatcher = sequential;
		});
		
		radioPanel.add(radioSequential);
		radioPanel.add(radioMostMatches);
		radioPanel.add(radioFastestMatch);
		radioPanel.add(radioDoublesMatch);
		
		inputPanel.add(radioPanel);
		
		JPanel inputFieldPanel = new JPanel();
		inputField = new JTextArea(20, 40);
		inputField.setText(DEFAULT_CODE);
		inputFieldPanel.add(inputField);
		inputPanel.add(inputFieldPanel);
		
		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(a -> {
			clientVrtl.sendMessage(GenericSourceTask.toMessage(getClassName(), inputField.getText()), selectedDispatcher);
		});
		inputFieldPanel.add(sendButton);
		
		JPanel outputPanel = new JPanel(); 
		outputPanel.setBorder(BorderFactory.createTitledBorder("Results:"));
		contentPane.add(outputPanel, BorderLayout.CENTER);
		
		messageArea = new JTextField(40); 
		outputPanel.add(new JScrollPane(messageArea));
		
		JPanel logPanel = new JPanel(); 
		logPanel.setBorder(BorderFactory.createTitledBorder("Log:"));
		contentPane.add(logPanel, BorderLayout.SOUTH);
		
		logArea = new JTextArea(10, 40); 
		logPanel.add(new JScrollPane(logArea));
		DefaultCaret caret = (DefaultCaret) logArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(a -> {
			messageArea.setText("");
			logArea.setText("");
		});
		logPanel.add(clearButton);
		
		frame.pack(); 
		frame.setLocation(200,  200);
		frame.setVisible(true); 
		
		vertx = Vertx.vertx(); 
		
		clientVrtl = new ClientVerticle(this);
		vertx.deployVerticle(clientVrtl, asyncResult -> {
			if (asyncResult.succeeded()) {
				log("Deployed client: " + asyncResult.result());
			} else {
				log("Deploying client failed: " + asyncResult.cause().toString());
			}
		});
	}
	
	private String getClassName() {
		String code = inputField.getText();
		int classPos = code.indexOf("class ");
		if(classPos != -1) {
			int end;
			for(end = classPos + 6; isClassChar(code.charAt(end)); end++) {	
			}
			return code.substring(classPos + 6, end);			
		}
		return "";
	}
	
	private boolean isClassChar(char c) {
		return c != ' ' && c != '{';
	}

	void log(String logMsg) {
		logArea.append(logMsg + "\n");
	}
}
