package tasks.common;

import java.util.function.Supplier;

import task.server.TaskResult;

public class Task<O> implements Supplier<TaskResult<O>> {
	
	private final Supplier<TaskResult<O>> supplier; 
	
	public Task(Supplier<TaskResult<O>> supplier) {
		this.supplier = supplier;
	}

	@Override
	public TaskResult<O> get() {
		try {
			return supplier.get();
		} catch(Throwable t) {
			return TaskResult.forFailedTask("Code threw an " + t.getClass().getName() + ": " + t.getMessage());
		}
	} 
}