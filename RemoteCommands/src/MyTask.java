import java.util.function.Supplier;

public class MyTask implements Supplier<String> {
    public String get() {
        return "asd1";
    }
}