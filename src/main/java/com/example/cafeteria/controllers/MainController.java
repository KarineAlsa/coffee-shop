package com.example.cafeteria.controllers;

import com.example.cafeteria.HelloApplication;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainController {

    @FXML
    protected void onStartSimulationButtonClick(MouseEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Parent root = FXMLLoader.load(Objects.requireNonNull(HelloApplication.class.getResource("cafeteria-view.fxml")));
        stage.getScene().setRoot(root);
    }

    @FXML
    protected void onExitButtonClick(MouseEvent event) {
        Platform.exit();
    }

}
