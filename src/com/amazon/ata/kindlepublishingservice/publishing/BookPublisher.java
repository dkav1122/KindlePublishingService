package com.amazon.ata.kindlepublishingservice.publishing;

import com.google.common.annotations.VisibleForTesting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Class responsible for executing publishing tasks. The publisher is created in an off state. A call to start will
 * start the publishing process.
 */
@Singleton
public class BookPublisher {

    private static final Logger log = LogManager.getLogger(BookPublisher.class);

    private final ScheduledExecutorService scheduledExecutorService;
    private final Runnable publishTask;
    private boolean isRunning;  //initialized to an off state

    /**
     * Instantiates a new BookPublisher object.
     *
     * @param scheduledExecutorService will schedule publishing tasks
     * @param publishTask the task that should be scheduled to publish books
     */
    @Inject
    public BookPublisher(ScheduledExecutorService scheduledExecutorService,
                         Runnable publishTask) {
        this.publishTask = publishTask;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    /**
     * Start publishing books.
     */
    public void start() {
        if (isRunning) {
            return;
        }
        isRunning = true;
        //calls Book Publish task run()
        scheduledExecutorService.scheduleWithFixedDelay(publishTask, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Stop publishing books.
     */
    public void stop() {
        isRunning = false;
        scheduledExecutorService.shutdown();
    }

    /**
     * Returns true if the publisher is currently working to publish books and false otherwise.
     * @return if the publisher is currently processing.
     */
    @VisibleForTesting
    boolean isRunning() {
        return isRunning;
    }
}
