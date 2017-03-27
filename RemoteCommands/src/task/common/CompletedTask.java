package task.common;

import task.server.TaskResult;


public class CompletedTask<O> extends Task<O> {

	public CompletedTask(TaskResult<O> result) {
		super(() -> result);
	}
}
