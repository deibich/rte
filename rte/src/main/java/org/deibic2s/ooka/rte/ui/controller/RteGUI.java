package org.deibic2s.ooka.rte.ui.controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class RteGUI extends Application {


    @Override public void init() {
    }

    @Override public void start(Stage stage) {
        Parent root = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainView.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MainViewController mainViewcontroller = loader.getController();
        mainViewcontroller.setStage(stage);

        Scene scene = new Scene(root, 700, 800);

        stage.setTitle("RTE");
        stage.setScene(scene);
        stage.show();
    }

    @Override public void stop() {
    }

    public static void main(String[] parameters) {
        launch(parameters);
    }

}