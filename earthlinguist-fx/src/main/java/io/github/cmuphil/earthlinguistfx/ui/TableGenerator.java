package io.github.cmuphil.earthlinguistfx.ui;

import io.github.cmuphil.earthlinguistfx.audio.AudioManager;
import io.github.cmuphil.earthlinguistfx.data.CheckboxRow;
import io.github.cmuphil.earthlinguistfx.data.TableData;
import io.github.cmuphil.earthlinguistfx.data.TableRow;
import io.github.cmuphil.earthlinguistfx.utils.UiUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;

/**
 * Generates a table view for a given example.
 *
 * @author josephramsey
 */
public class TableGenerator {

    /**
     * The height of the example images.
     */
    private double exampleImageHeight = 110;

    /**
     * The height of the checkbox images.
     */
    private double checkboxImageHeight = 50;

    /**
     * The audio file directory.
     */
    private File audioFileDir;

    /**
     * Creates a table view for a given example.
     *
     * @param example the example.
     * @return the table view.
     */
    public TableView<TableRow> getExampleTable(TableData example, double exampleImageHeight,
                                               double checkboxImageHeight, boolean record,
                                               String audioFileDir, AppState appState) {
        this.audioFileDir = new File(audioFileDir);

        setCheckboxImageHeight(checkboxImageHeight);
        setExampleImageHeight(exampleImageHeight);

        ObservableList<TableRow> rows = FXCollections.observableArrayList();
        rows.addAll(example.getRows());

        TableView<TableRow> tableView = new TableView<>();
        tableView.setItems(rows);
        tableView.setSelectionModel(null);

        // First column setup
        TableColumn<TableRow, String> firstColumn = getFirstColumn();
        firstColumn.setReorderable(false);
        firstColumn.setSortable(false);

        tableView.getColumns().add(firstColumn);

        // Rest of the columns
        for (int i = 0; i < example.getNumColumns(); i++) {  // Assuming 3 columns for this example
            TableColumn<TableRow, String> column = getCheckboxColumn(i, record, appState);
            column.setReorderable(false);
            column.setSortable(false);
            tableView.getColumns().add(column);
        }

        return tableView;
    }

    /**
     * Creates a checkbox column for the table
     *
     * @param i        the index of the column
     * @param record   true if recording is allowed
     * @param appState the app state
     * @return the checkbox column
     */
    private TableColumn<TableRow, String> getCheckboxColumn(int i, boolean record, AppState appState) {
        final BooleanProperty isRecording = new SimpleBooleanProperty(false);

        TableColumn<TableRow, String> column = new TableColumn<>(String.valueOf(i));

        HBox controlButtons = AudioManager.getInstance().createControlButtons(isRecording, i, audioFileDir,
                record, appState);
        VBox headerItem = new VBox();
        headerItem.getChildren().add(new Label("" + (i + 1)));
        headerItem.getChildren().add(controlButtons);
        headerItem.setAlignment(Pos.CENTER);
        column.setGraphic(headerItem);

        column.setText(null);
        column.setMinWidth(120);
        column.setCellValueFactory(cellData ->
                new SimpleStringProperty(((CheckboxRow) cellData.getValue()).getImage(i)));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    try {
                        setGraphic(UiUtils.createImage(item, checkboxImageHeight));
                    } catch (Exception e) {
                        try {
                            setGraphic(UiUtils.createImage(new File(item), checkboxImageHeight));
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    setGraphic(UiUtils.createImage(item, checkboxImageHeight));
                } else {
                    setGraphic(null);
                    setText(null);
                }
            }
        });
        return column;
    }

    /**
     * Creates the first column for the table, which contains the example images, which users will be asked to record
     * descriptive audio for. The idea is they should give audio clips that describe the situation where all the rows in
     * the column that are checked are true.
     *
     * @return the first column
     */
    private TableColumn<TableRow, String> getFirstColumn() {
        TableColumn<TableRow, String> firstColumn = new TableColumn<>();
        firstColumn.setCellValueFactory(cellData -> new SimpleStringProperty(((CheckboxRow) cellData.getValue()).getExampleImage()));
        firstColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (!empty) {
                    StackPane image;

                    try {
                        image = UiUtils.createImage(item, exampleImageHeight);
                    } catch (Exception e) {
                        try {
                            image = UiUtils.createImage(new File(item), exampleImageHeight);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }

                    setGraphic(image);
                } else {
                    setGraphic(null);
                    setText(null);
                }
            }
        });
        return firstColumn;
    }

    /**
     * Sets the example image height.
     *
     * @param exampleImageHeight the example image height.
     */
    private void setExampleImageHeight(double exampleImageHeight) {
        if (exampleImageHeight < 1) {
            throw new IllegalArgumentException("Example image height must be at least 1.");
        }
        this.exampleImageHeight = exampleImageHeight;
    }

    /**
     * Sets the checkbox image height.
     *
     * @param checkboxImageHeight the checkbox image height.
     */
    private void setCheckboxImageHeight(double checkboxImageHeight) {
        if (exampleImageHeight < 1) {
            throw new IllegalArgumentException("Checkmark image height must be at least 1.");
        }
        this.checkboxImageHeight = checkboxImageHeight;
    }
}
