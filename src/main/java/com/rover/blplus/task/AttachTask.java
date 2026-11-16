package com.rover.blplus.task;

import com.rover.blplus.process.exception.AttachException;
import com.rover.blplus.process.ProcessManager;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttachTask extends Task<Boolean> {

    private final Logger log = LoggerFactory.getLogger(AttachTask.class);

    public AttachTask() {
        super();
    }

    @Override
    protected Boolean call() throws Exception {
        boolean attached = false;
        int maxTries = 10;
        int currentTry = 0;

        while (!attached && currentTry < maxTries) {
            log.trace("Attach current try = {}", currentTry);
            try {
                ProcessManager.getInstance().attach();
                attached = true;
            } catch(AttachException e) {
                currentTry++;
                log.warn("Could not attach to process", e);
                try {
                    Thread.sleep(3000);
                } catch(InterruptedException ignored) {
                }
            }
        }

        return attached;
    }
}
