package com.rover.blplus.gui;

import com.rover.blplus.controller.AttachController;
import com.rover.blplus.process.hack.HackThread;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class MainApp extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;

        primaryStage.setOnCloseRequest(windowEvent -> exit());

        FXMLLoader loader = new FXMLLoader();
        AttachController controller = new AttachController(this);
        loader.setController(controller);
        loader.setLocation(getClass().getClassLoader().getResource("fxml/attach.fxml"));
        Parent root = loader.load();

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Borderlands+");
        primaryStage.show();
    }

    public Stage getPrimaryStage() {
        return this.primaryStage;
    }

    public void changeScene(Object controller, URL location) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setController(controller);
        loader.setLocation(location);
        Parent root = loader.load();

        primaryStage.setScene(new Scene(root));
    }

    public void openWindow(Object controller, URL location, String title, boolean wait) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setController(controller);
        loader.setLocation(location);
        Parent root = loader.load();

        Stage window = new Stage();
        window.setTitle(title);
        window.setScene(new Scene(root));
        window.initOwner(primaryStage);
        if (!wait) {
            window.show();
        } else {
            window.showAndWait();
        }
    }

    public void showError(String header, String message) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Borderlands+ - Error");
        errorAlert.setContentText(message);
        errorAlert.setHeaderText(header);
        errorAlert.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(getClass().getClassLoader().getResource("css/theme.css")).toExternalForm());
        errorAlert.showAndWait();
    }

    public void exit() {
        exit(null, null);
    }

    public void exit(String header, String message) {

        if (header != null && message != null) {
            showError(header, message);
        }

        Platform.exit();
        System.exit(0);
    }
}
