package com.rover.blplus;

import com.rover.blplus.gui.MainApp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static javafx.application.Application.launch;

public class Launcher {

    private static final Logger log = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        try {
            log.info("------------------------------ APP STARTED ------------------------------");
            launch(MainApp.class, args);
        } catch(Exception e) {
            log.error("", e);
        }
    }

}
