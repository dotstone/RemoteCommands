package task.server;

public class TaskResult<T> {

	private final T result;
	private final String error;
	
	private TaskResult(T result, String error) {
		this.result = result;
		this.error = error;
	}
	public TaskResult(T result) {
		this(result, null);
	}
	
	public static <T>TaskResult<T> forFailedTask(String error) {
		return new TaskResult<T>(null, error);
	}
	
	public T getResult() {
		return result;
	}
	
	public String getError() {
		return error;
	}
	
	public boolean isValid() {
		return error == null;
	}
}
