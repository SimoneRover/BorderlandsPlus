package com.rover.blplus.task;

import javafx.concurrent.Task;

public class WaitTask extends Task<Void> {

    private final long duration;

    public WaitTask(long duration) {
        super();
        this.duration = duration;
    }

    @Override
    protected Void call() throws Exception {
        try {
            Thread.sleep(duration);
        } catch(InterruptedException ignored) {
        }

        return null;
    }
}
