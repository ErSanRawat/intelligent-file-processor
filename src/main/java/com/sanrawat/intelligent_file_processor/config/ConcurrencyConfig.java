package com.sanrawat.intelligent_file_processor.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.concurrent.*;


/**
 * Concurrency configuration class.
 * - Provides distinct executors for CPU-bound and IO-bound tasks.
 * - Uses a fixed thread pool for CPU heavy work (parsing, transformations).
 * - Uses virtual threads (JDK 25) for IO-bound tasks where blocking calls are made.
 * - Provides a scheduled executor for periodic tasks like batch flushes.
 */
@Configuration
public class ConcurrencyConfig {


    @Bean(destroyMethod = "shutdown")
    public ExecutorService cpuBoundPool() {
        int cores = Math.max(2, Runtime.getRuntime().availableProcessors());
// Fixed pool sized to CPU cores to prevent oversubscription for CPU-bound tasks
        return Executors.newFixedThreadPool(cores, runnable -> {
            Thread t = new Thread(runnable);
            t.setName("cpu-pool-" + t.getId());
            t.setDaemon(false);
            return t;
        });
    }


    @Bean(destroyMethod = "shutdown")
    public ExecutorService ioBoundExecutor() {
// Virtual threads are ideal for many concurrent blocking IO tasks.
        return Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("virt-io-", 0).factory());
    }


    @Bean
    public ScheduledExecutorService scheduledExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }


    @Bean
    public BlockingQueue<java.nio.file.Path> ingestionQueue() {
// Bounded queue provides backpressure between file watcher and ingest coordinator
        return new ArrayBlockingQueue<>(1000);
    }
}