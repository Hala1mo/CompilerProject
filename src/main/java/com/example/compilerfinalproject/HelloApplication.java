package com.example.compilerfinalproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

import javafx.application.Application;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.fxml.FXMLLoader;
import java.io.File;
import java.io.IOException;

import javafx.application.Application;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import java.io.File;
import java.util.List;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");

        Button button = new Button("Open File");
        button.setOnAction(e -> {
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                System.out.println("Selected file: " + selectedFile.getAbsolutePath());
                Scanner scanner = new Scanner(selectedFile);
               List<Token> tokens=scanner.Tokenizer();
                for (Token token : tokens) {
                    System.out.println(token.toString());
                }
              Parser p =new Parser(tokens);
            }
        });


        StackPane root = new StackPane();
        root.getChildren().add(button);
        Scene scene = new Scene(root, 320, 240);

        // Set the scene and show the stage
        stage.setScene(scene);
        stage.setTitle("File Explorer Example");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}