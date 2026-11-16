package com.rover.blplus.controller;

import com.rover.blplus.Addresses;
import com.rover.blplus.gui.MainApp;
import com.rover.blplus.process.exception.ProcessException;
import com.rover.blplus.task.AttachTask;

import com.rover.blplus.task.WaitTask;
import com.rover.utility.os.OS;
import com.rover.utility.os.OSUtils;
import com.rover.utility.process.BrokenPointerChainException;
import com.sun.jna.Pointer;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class AttachController {

    private final Logger log = LoggerFactory.getLogger(AttachController.class);

    @FXML
    private Button button;
    @FXML
    private Label label;
    @FXML
    private ImageView image;

    private final Image spinner;
    private final RotateTransition loading;
    private final MainApp app;

    public AttachController(MainApp app) {
        this.app = app;
        spinner = new Image( Objects.requireNonNull( getClass().getClassLoader().getResourceAsStream("assets/spinner.png") ));
        loading = new RotateTransition();
        loading.setDuration(Duration.millis(1000));
        loading.setInterpolator(Interpolator.LINEAR);
        loading.setDelay(Duration.millis(0));
        loading.setByAngle(360);
        loading.setCycleCount(RotateTransition.INDEFINITE);
    }

    @FXML
    public void initialize() {
        image.setImage(spinner);
        loading.setNode(image);
        checkOS();
        attach();
    }

    public void checkOS() {
        label.setText("Checking system compatibility");
        showLoading();

        OS operatingSys = OSUtils.getOS();
        log.info("Detected OS: {}", operatingSys.name());
        if (!operatingSys.equals(OS.WINDOWS)) {
            this.app.exit("Incompatible OS", "Your operating system is not supported.");
        }
    }

    public void attach() {
        label.setText("Looking for \"Borderlands GOTY Enhanced\" window");
        showLoading();

        AttachTask task = new AttachTask();
        task.setOnSucceeded(workerStateEvent -> {
            boolean attached = task.getValue();
            if (attached) {
                // Open trainer window
                log.trace("Attached to process");
                label.setText("Attached to \"Borderlands GOTY Enhanced\" process");
                hideLoading();

                try {
                    this.app.changeScene(new MainController(app), getClass().getClassLoader().getResource("fxml/main.fxml"));
                } catch(IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                log.trace("Borderlands process not found or an error occurred during attach process");
                label.setText("Could not attach to \"Borderlands GOTY Enhanced\" process");
                showButton();
            }
        });
        task.setOnFailed(workerStateEvent -> {
            log.trace("Attach task failed");
            label.setText("Could not attach to \"Borderlands GOTY Enhanced\" process");
            showButton();
        });
        Thread t = new Thread(task, "attach-thread");
        t.setDaemon(true);
        log.trace("Starting attach task");
        t.start();
    }

    private void showLoading() {
        button.setManaged(false);
        button.setVisible(false);

        image.setManaged(true);
        image.setVisible(true);
        loading.play();
    }

    private void showButton() {
        image.setManaged(false);
        image.setVisible(false);
        loading.stop();

        button.setManaged(true);
        button.setVisible(true);
    }

    private void hideLoading() {
        image.setManaged(false);
        image.setVisible(false);
        loading.stop();
    }
}
