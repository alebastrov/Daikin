import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class GeneratorUUID {

    private interface Generator extends AutoCloseable {
        String produce() throws InterruptedException;
        default int size() {return 0;}
        default void close() throws Exception {}
    }

    public static class RealGeneratorUUID implements Generator, AutoCloseable {
        @Override
        public String produce() {
            return UUID.randomUUID().toString();
        }
    }

    public static class BufferedGeneratorUUID implements Generator, AutoCloseable {
        public static final int CAPACITY = 10_000_000;
        private ArrayBlockingQueue<String> generatedUUIDs = new ArrayBlockingQueue<String>(CAPACITY);
        private Generator generator;
        private volatile boolean stop = false;
        private Thread generation = new Thread(() -> {
            Thread.currentThread().setName("Buffered UUID generator, size: " + CAPACITY);
            while (!stop) {
                try {
                    generatedUUIDs.offer(UUID.randomUUID().toString(), 1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Shutdown detected. Generator of UUID is stopped.");
                    break;
                }
            }
        });

        public BufferedGeneratorUUID(Generator generator) {
            if (generator == null) throw new IllegalArgumentException();
            this.generator = generator;
            generation.start();
        }

        public String produce() throws InterruptedException {
            String result = generatedUUIDs.poll(1, TimeUnit.MILLISECONDS);
            if (result == null) return generator.produce();
            return result;
        }

        @Override
        public void close() throws Exception {
            stop = true;
        }

        public int size() {
            return generatedUUIDs.size();
        }
    }

    public static void main(String[] args) throws Exception {
        try(Generator uuidGenerator1 = new RealGeneratorUUID();
            Generator uuidGenerator2 = new BufferedGeneratorUUID(uuidGenerator1);) {
            System.err.println("sleeping...");
            Thread.sleep(20_000);
            System.err.println("running..., size: "+uuidGenerator2.size());

            long time = System.currentTimeMillis();
            String old = UUID.randomUUID().toString();
            ExecutorService service = Executors.newFixedThreadPool(16);
            final AtomicInteger counter = new AtomicInteger();
            for (int i = 0; i < 10_000; i++) {
                service.submit(() -> {
                    for (int j = 0; j < 10_000; j++) {
                        if (counter.incrementAndGet() % 2_000_000 == 0) {
                            System.err.println("#" + counter.get() + ". Buffer: " + uuidGenerator2.size());
                        }
                        try {
                            if (old.equals(uuidGenerator2.produce())) {
                                System.err.println("break! Time=" + (System.currentTimeMillis() - time) + " ms for #" + counter.get());
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.err.println("Stop detected");
                            service.shutdownNow();
                        }
                    }
                });
            }
            service.shutdown();
            System.err.println("===WAIT FOR FINISH===");
            service.awaitTermination(10, TimeUnit.MINUTES);
            System.err.println("finish! Time=" + (System.currentTimeMillis() - time) + " ms");
        }
    }
}
