package task.client.samples;

import java.util.Random;
import java.util.function.Supplier;

public class RandomNumberTask implements Supplier<String>{

    public String get() {
        return String.valueOf(new Random().nextInt(3));
    }
}
