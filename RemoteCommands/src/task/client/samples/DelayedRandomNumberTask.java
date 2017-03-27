package task.client.samples;

import java.util.Random;
import java.util.function.Supplier;

public class DelayedRandomNumberTask implements Supplier<String>{

    public String get() {
        try {
            Thread.sleep(new Random().nextInt(5000));
        } catch(InterruptedException e) {
            return "Interrupted";
        }
        return String.valueOf(new Random().nextInt(3));
    }
}