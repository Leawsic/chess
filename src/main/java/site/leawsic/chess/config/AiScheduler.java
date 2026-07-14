package site.leawsic.chess.config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public final class AiScheduler {
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "chess-ai");
        thread.setDaemon(true);
        return thread;
    });

    private AiScheduler() {
    }

    public static void think(Runnable task) {
        long delay = ThreadLocalRandom.current().nextLong(300L, 751L);
        EXECUTOR.schedule(task, delay, TimeUnit.MILLISECONDS);
    }
}
