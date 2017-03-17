package tasks.common;

import java.util.function.Supplier;

import net.openhft.compiler.CompilerUtils;
import task.server.TaskResult;

public class GenericSourceTask<O> extends Task<O> {
	
	public GenericSourceTask(Supplier<O> supplier) {
		super(() -> new TaskResult<O>(supplier.get()));
	}

	public static <O>Task<O> fromFunctionSource(String className, String src) {
		try {
			Class<? extends Supplier<?>> aClass = CompilerUtils.CACHED_COMPILER.loadFromJava(className, src);
			Supplier<O> supplier = (Supplier<O>) aClass.newInstance();
			return new GenericSourceTask<O>(() -> supplier.get());
		} catch (ClassNotFoundException e) {
			System.out.println("Class " + className + " was not found!");
			return new CompletedTask<O>(TaskResult.forFailedTask("Cannot find class " + className));
		} catch (InstantiationException e) {
			System.err.println("Cannot instantiate object!");
			return new CompletedTask<O>(TaskResult.forFailedTask("Cannot instantiate object of class " + className));
		} catch (IllegalAccessException e) {
			System.err.println("Cannot access class!");
			return new CompletedTask<O>(TaskResult.forFailedTask("Illegal access on class " + className));
		}
	}
	
	public static <O>Task<O> fromMessage(String message) {
		String[] parts = message.split(":", 2);
		if(parts.length != 2) {
			throw new IllegalArgumentException("Invalid message: " + message);
		}
		String className = parts[0];
		String source = parts[1];
		return fromFunctionSource(className, source);
	}
}