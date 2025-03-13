package io.github.cmuphil.earthlinguistfx;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * An application that allows the user to add and remove images from a list.
 *
 * @author josephramsey
 */
public class ImageListApp extends Application {

    /**
     * The list of image views.
     */
    private final ObservableList<ImageView> imageViews = FXCollections.observableArrayList();

    /**
     * The tile pane.
     */
    private final TilePane tilePane = new TilePane();

    /**
     * The main method.
     *
     * @param args the command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Starts the application.
     *
     * @param primaryStage the primary stage.
     */
    @Override
    public void start(Stage primaryStage) {
        tilePane.setPrefColumns(3);
        tilePane.setAlignment(Pos.CENTER);

        Button addButton = new Button("Add Image");
        addButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                try {
                    Image image = new Image(new FileInputStream(selectedFile));
                    ImageView imageView = new ImageView(image);
                    imageView.setPreserveRatio(true);
                    imageView.setFitHeight(100); // Set the desired height, for example 100px
                    imageViews.add(imageView);
                    tilePane.getChildren().add(imageView);
                } catch (FileNotFoundException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(null);
                    alert.setHeaderText(null);
                    alert.setContentText("The file you selected could not be found.");
                    alert.showAndWait();
                }
            }
        });

        Button removeButton = new Button("Remove Selected Image");
        removeButton.setOnAction(e -> {
            if (!imageViews.isEmpty()) {
                ImageView imageView = imageViews.remove(imageViews.size() - 1);
                tilePane.getChildren().remove(imageView);
            }
        });

        VBox root = new VBox(10, tilePane, addButton, removeButton);
        Scene scene = new Scene(root, 400, 600);
        primaryStage.setTitle("Image List App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

