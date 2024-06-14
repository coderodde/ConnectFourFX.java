package com.github.coderodde.game.connect4fx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author PotilasKone
 */
public class ConnectFourFX extends Application {
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("rodde's Connect 4");
        
        final StackPane root = new StackPane();
        final ProgressBar progressBar = new ProgressBar();
        final Canvas canvas = new ConnectFourFXCanvas(progressBar);
        final VBox vbox = new VBox();
        
        vbox.getChildren().addAll(canvas, progressBar);
        
        root.getChildren().add(vbox);
        stage.setScene(new Scene(root));
        
        stage.setResizable(false);
        stage.show();
    }
}
