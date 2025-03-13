package io.github.cmuphil.earthlinguistfx.utils;

import io.github.cmuphil.earthlinguistfx.data.TableData;
import io.github.cmuphil.earthlinguistfx.data.TableRow;
import io.github.cmuphil.earthlinguistfx.examples.Examples;
import io.github.cmuphil.earthlinguistfx.ui.AppState;
import io.github.cmuphil.earthlinguistfx.ui.EarthLinguist;
import io.github.cmuphil.earthlinguistfx.ui.TableGenerator;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Various static utility method for the EarthLinguist UI.
 *
 * @author josephramsey
 */
public class UiUtils {

    /**
     * Creates an image view for the given file path.
     *
     * @param filePath the file path.
     * @param height   the height of the image.
     */
    public static StackPane createImage(String filePath, double height) {
        StackPane pane = new StackPane();

        try (InputStream resourceStream = EarthLinguist.class.getResourceAsStream(filePath)) {
            if (resourceStream == null) {
                throw new RuntimeException("Couldn't load image: " + filePath);
            }

            ImageView imageView = new ImageView(new Image(resourceStream));
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(height);
            pane.getChildren().add(imageView);
            return pane;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static StackPane createImage(File file, double height) throws IOException {
        StackPane pane = new StackPane();
        Image image = new Image(new FileInputStream(file));
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(height);
        pane.getChildren().add(imageView);
        return pane;
    }

    /**
     * Returns the file where the audio is saved, for a given checkmark column.
     *
     * @param audioFileDir the directory where the audio files are saved.
     * @param columnIndex  the column number.
     * @return the file where the audio is saved, for a given checkmark column.
     */
    public static File getSoundFile(File audioFileDir, int columnIndex) {
        return new File(audioFileDir, "clip." + (columnIndex + 1) + ".wav");
    }

    /**
     * Returns a formatted TableView for the given example. The 'record' parameter governs whether the table is meant
     * for recording clips or listening to clips.
     *
     * @param record       whether the table is meant for recording clips or listening to clips.
     * @param exampleIndex the index of the example.
     * @param dir          the directory.
     * @return a formatted TableView for the given example.
     */
    public static TableView<TableRow> getTable(boolean record, int exampleIndex, AppState appState, String dir) {
        TableData tableData = Examples.getInstance().getExample(exampleIndex);
        TableGenerator tableGenerator = new TableGenerator();

        TableView<TableRow> tableView = tableGenerator.getExampleTable(tableData, 110,
                50, record, dir, appState);
        tableView.setPrefSize(800, 600);

        return tableView;
    }

    /**
     * Returns a formatted Region as a Pane.
     *
     * @param region the region.
     * @return a formatted Region as a Pane.
     */
    public static Pane getRegion(Region region) {
        HBox hBox8 = new HBox();
        hBox8.getChildren().add(region);
        hBox8.setPadding(new Insets(10));
        return hBox8;
    }

    /**
     * Returns a Pane with the given ImageView centered.
     *
     * @param imageView the ImageView.
     * @return a Pane with the given ImageView centered.
     */
    public static Pane getCenteredImage(ImageView imageView) {
        return new StackPane(imageView);
    }

    /**
     * Returns a labeled Region as a Pane.
     */
    public static Pane getLabeledRegion(String text, Region... region) {
        Label label = new Label(text);
        label.setFont(new Font(16));
        label.setWrapText(true);
        HBox hBox = new HBox(label);
        for (Region r : region) {
            hBox.getChildren().add(r);
        }
        hBox.setSpacing(10);
        hBox.setPadding(new Insets(10));
        return new StackPane(hBox);
    }

    /**
     * Returns a formatted Label as a Pane.
     *
     * @param text the text of the label.
     * @return a formatted Label as a Pane.
     */
    public static StackPane getLabel(String text) {
        Label label = new Label(text);
        label.setFont(new Font(16));
        label.setWrapText(true);
        HBox hBox = new HBox(label);
        hBox.setPadding(new Insets(10));
        return new StackPane(hBox);
    }

    // Deletes and recreates the given file.
    public static void recreateFile(String clipsFilename, File file) {
        if (file.exists()) {
            boolean delete = file.delete();

            if (!delete) {
                alert(Alert.AlertType.ERROR, "Error deleting file: " + clipsFilename);
                throw new RuntimeException("Error deleting file: " + clipsFilename);
            }
        }
        try {
            boolean newFile = file.createNewFile();

            if (!newFile) {
                alert(Alert.AlertType.ERROR, "Error creating file: " + clipsFilename);
                throw new RuntimeException("Error creating file: " + clipsFilename);
            }
        } catch (IOException ex) {
            alert(Alert.AlertType.ERROR, "Error creating file: " + clipsFilename);
            throw new RuntimeException("Error creating file: " + clipsFilename);
        }
    }

    /**
     * Returns a user area for the given index, for the annotation tab. If editable is true, the user area will be
     * editable. Otherwise, it will not be editable.
     *
     * @param i          the index.
     * @param editable   whether the user area should be editable.
     * @param appState   the app state.
     * @param directory  the directory.
     * @param numColumns the number of columns.
     * @return a user area for the given index.
     */
    public static TextArea getUserArea(int i, boolean editable, AppState appState, String directory, int numColumns) {
        TextArea userArea = new TextArea();
        userArea.setPromptText("Enter your description here");
        userArea.setText(i < appState.getUserAnnotations().length ? appState.getUserAnnotations()[i] : "");
        userArea.setPrefRowCount(2);
        userArea.setPrefColumnCount(55);
        userArea.setMaxHeight(30);

        if (editable) {
            userArea.setWrapText(true);
            userArea.setEditable(true);
            userArea.getStyleClass().add("text-userArea-enabled");
            userArea.textProperty().addListener((observable, oldValue, newValue) -> {
                appState.getUserAnnotations()[i] = newValue;
                try {
                    AppState.toJson(appState, directory);
                } catch (IOException e) {
                    new Alert(Alert.AlertType.ERROR, "Error saving annotations").showAndWait();
                    throw new RuntimeException(e);
                }
            });
        } else {
            userArea.setEditable(false);
            userArea.getStyleClass().add("text-userArea-disabled");
        }

        return userArea;
    }

    /**
     * Returns an expert area for the given index, for the annotation tab. If editable is true, the expert area will be
     * editable. Otherwise, it will not be editable.
     *
     * @param i          the index.
     * @param editable   whether the expert area should be editable.
     * @param appState   the app state.
     * @param directory  the directory.
     * @param numColumns the number of columns.
     * @return an expert area for the given index.
     */
    public static TextArea getExpertArea(int i, boolean editable, AppState appState, String directory, int numColumns) {
        TextArea expertArea = new TextArea();
        expertArea.setPromptText("For experts: Enter additional annotations here");
        expertArea.setText(i < appState.getExpertAnnotations().length ? appState.getExpertAnnotations()[i] : "");
        expertArea.setPrefRowCount(2);
        expertArea.setPrefColumnCount(55);
        expertArea.setMaxHeight(30);
        expertArea.setWrapText(true);

        if (editable) {
            expertArea.setEditable(true);
            expertArea.getStyleClass().add("text-userArea-enabled");
            expertArea.textProperty().addListener((observable, oldValue, newValue) -> {
                appState.getExpertAnnotations()[i] = newValue;
                try {
                    AppState.toJson(appState, directory);
                } catch (IOException e) {
                    new Alert(Alert.AlertType.ERROR, "Error saving annotations").showAndWait();
                    throw new RuntimeException(e);
                }
            });
        } else {
            expertArea.setEditable(false);
            expertArea.getStyleClass().add("text-userArea-disabled");
        }

        return expertArea;
    }

    /**
     * Returns an instance of ComboBoxes filled with the respective values for examples, countries and languages.
     *
     * @return an instance of ComboBoxes filled with the respective values for examples, countries and languages.
     */
    public static EarthLinguist.comboBoxes getSetUpComboBoxes() {
        ComboBox<Integer> examplesCombo = new ComboBox<>();
        ComboBox<String> countriesCombo = new ComboBox<>();
        ComboBox<String> languagesCombo = new ComboBox<>();

        List<Integer> examples = Examples.getInstance().getExampleIndices();

        String filePath2 = "/app_settings/languages.txt";
        List<String> languages = readStringsFromFile(filePath2);
        Collections.sort(languages);

        String filePath3 = "/app_settings/countries.txt";
        List<String> countries = readStringsFromFile(filePath3);
        Collections.sort(countries);

        examplesCombo.setPromptText("Select an example");
        examplesCombo.getItems().clear();
        examplesCombo.getItems().addAll(examples);

        languagesCombo.setPromptText("Select a language");
        languagesCombo.getItems().clear();
        languagesCombo.getItems().addAll(languages);

        countriesCombo.setPromptText("Select a country");
        countriesCombo.getItems().clear();
        countriesCombo.getItems().addAll(countries);

        countriesCombo.setValue(countries.get(0));
        languagesCombo.setValue(languages.get(0));
        examplesCombo.setValue(examples.get(0));

        return new EarthLinguist.comboBoxes(examplesCombo, countriesCombo, languagesCombo);
    }

    /**
     * Initializes an AppState object by extracting the application state from a specified zip file and performing
     * validation on the extracted contents.
     * <p>
     * The process includes the following steps: 1. Clearing the existing contents of the specified directory to prepare
     * for the new data. 2. Extracting the contents of the zip file into the directory, which includes potentially the
     * state configuration ("state.json") and audio clip files. 3. Validating the extracted contents by ensuring the
     * presence of a "state.json" file for AppState initialization, checking for the absence of subdirectories and
     * hidden files, and verifying that all present audio clip files are named according to a specific format
     * ("clip.[number].wav") and are valid for use in the application. 4. Initializing the AppState object based on the
     * "state.json" file, which contains necessary configuration or state information for the application.
     * <p>
     * This method aims to ensure the integrity and correctness of the application state and audio files extracted from
     * the zip. If the "state.json" file is missing, the directory is inaccessible, contains invalid files, or if any
     * other error occurs during the extraction and validation process, the method attempts to clean up by clearing the
     * directory contents again and displays an error message to the user. Depending on the nature of the error, the
     * method may return null (in cases where exceptions are caught and handled) or throw an IllegalArgumentException
     * for specific validation failures.
     * <p>
     * It is critical that the audio clip files follow the naming convention "clip.[number].wav", where [number]
     * represents a positive integer, ensuring they can be correctly associated with specific columns or elements within
     * the application's data structure.
     *
     * @param file        The zip file containing the necessary files for initializing the application state.
     * @param toDirectory The target directory where the contents of the zip file are to be extracted. This directory's
     *                    existing contents are cleared at the beginning of the process.
     * @return An AppState object initialized from the "state.json" file, or null if any error prevents successful
     * initialization.
     * @throws IOException              If an I/O error occurs during the extraction of the zip file.
     * @throws IllegalArgumentException If critical validation checks fail, such as the absence of the "state.json"
     *                                  file, inability to access the specified directory, invalid structure of the zip
     *                                  file, or presence of invalid files within the directory.
     */
    public static AppState loadAppState(File file, String toDirectory) throws IOException, IllegalArgumentException {
        ZipUtils.deleteDirectoryContents(new File(toDirectory));

        if (!new File(toDirectory).exists()) {
            if (!new File(toDirectory).mkdirs()) {
                throw new IOException("Could not create directory: " + toDirectory);
            }
        }

        ZipUtils.unzip(file, new File(toDirectory));

        if (new File(toDirectory).exists()) {
            checkClipsDirectory(toDirectory);
        } else {
            throw new IllegalArgumentException("Error loading state from file: " + file.getAbsolutePath());
        }

        return AppState.fromJson(toDirectory);
    }

    /**
     * Validates the contents of a specified directory for compliance with the expected structure and file format for
     * audio clips.
     *
     * <p>This method ensures that the directory contains a 'state.json' configuration file and that all audio
     * clips adhere to a specific naming convention and format. It checks for the absence of subdirectories and hidden
     * files within the directory. Each audio clip file must be named according to the pattern 'clip.<number>.wav',
     * where <number> is a positive integer that corresponds to a valid column in a specified example from the
     * application's state. The method also verifies the validity of each audio file.</p>
     *
     * @param toDirectory A {@link String} representing the path to the directory whose contents are to be validated.
     *                    The directory must contain a 'state.json' file and comply with the specified constraints
     *                    regarding subdirectories, hidden files, and clip file naming and format.
     * @throws IOException              If an I/O error occurs during the execution of the method.
     * @throws IllegalArgumentException If any of the following conditions are met:
     *                                  <ul>
     *                                  <li>The 'state.json' file does not exist in the specified directory.</li>
     *                                  <li>The specified directory does not exist or cannot be accessed.</li>
     *                                  <li>There are subdirectories within the specified directory.</li>
     *                                  <li>There are hidden files within the specified directory.</li>
     *                                  <li>A file in the directory does not match the expected naming convention and format for clip files.</li>
     *                                  <li>A clip file does not correspond to a column in the specified example within the application state.</li>
     *                                  <li>A clip file is not a valid audio file.</li>
     *                                  </ul>
     */
    public static void checkClipsDirectory(String toDirectory) throws IOException {
        if (!new File(toDirectory, "state.json").exists()) {
            throw new IllegalArgumentException("I was expecting a state.json file plus a number of clip.wav files.");
        }

        File[] files = new File(toDirectory).listFiles();

        if (files == null) {
            throw new IllegalArgumentException("The directory does not exist: " + toDirectory);
        }

        // Check that the app state can be parsed.
        AppState appState;

        try {
            appState = AppState.fromJson(toDirectory);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not parse app state: " + e.getMessage());
        }

        for (File _file : files) {
            if (_file.isDirectory()) {
                throw new IllegalArgumentException("The directory contains subdirectories.");
            }

            if (_file.getName().startsWith(".")) {
                throw new IllegalArgumentException("The directory contains hidden files.");
            }

            if (_file.getName().equals("state.json")) {
                continue;
            }

            // Any other file should be a clip.
            int clipNumber = getClipNumber(_file);
            int example = appState.getSelectedIndex();

            // It should be for a column in the given example.
            if (!(clipNumber >= 1 && clipNumber <= Examples.getInstance().getExample(example).getNumColumns())) {
                throw new IllegalArgumentException("The directory contains a clip that is not for a column in the given example, clip." + clipNumber + ".wav");
            }

            // It should be a valid audio file.
            try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(_file)) {
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.close();
            } catch (Exception ex) {
                throw new IllegalArgumentException("The directory contains a clip that is not a valid audio clip, clip." + clipNumber + ".wav");
            }
        }
    }

    /**
     * Returns the number of the clip from the given file.
     *
     * @param _file the file.
     * @return the number of the clip from the given file.
     */
    private static int getClipNumber(File _file) {
        String name = _file.getName();
        String[] tokens = name.split("\\.");

        if (tokens.length != 3 || !tokens[0].equals("clip") || !tokens[2].equals("wav") || !tokens[1].matches("\\d+") || Integer.parseInt(tokens[1]) < 1) {
            throw new IllegalArgumentException("The directory contains a clip that is not for a column in the given example, " + _file.getName());
        }

        return Integer.parseInt(tokens[1]);
    }

    /**
     * Shows an alert dialog with the given message.
     *
     * @param s the message.
     */
    public static void alert(Alert.AlertType alertType, String s) {
        Alert alert = new Alert(alertType);
        alert.setTitle(null);
        alert.setHeaderText(null);
        alert.setContentText(s);
        alert.showAndWait();
    }

    /**
     * Returns the current date and time as a formatted string.
     *
     * @return the current date and time as a formatted string.
     */
    public static String formatCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        return Constants.FORMATTER.format(now);
    }

    /**
     * Returns a formatted Region as a Pane.
     *
     * @param filePath the file path.
     * @return a formatted Region as a Pane.
     */
    public static List<String> readStringsFromFile(String filePath) {
        List<String> lines = new ArrayList<>();

        try (InputStream resourceStream = UiUtils.class.getResourceAsStream(filePath)) {
            if (resourceStream == null) {
                alert(Alert.AlertType.ERROR, "Problem reading file: " + filePath);
                return new ArrayList<>();
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
                return lines;
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "Error reading file: " + filePath).showAndWait();
                return lines;
            }
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load resource: " + filePath);
        }
    }

    /**
     * Returns true if some column has not been inverted.
     *
     * @param selectedExampleIndex the name of the example.
     * @return true if some column has not been inverted.
     */
    public static boolean someColumnNotRecorded(Integer selectedExampleIndex) {
        TableData table = Examples.getInstance().getExample(selectedExampleIndex);
        int numColumns = table.getNumColumns();

        for (int i = 0; i < numColumns; i++) {
            File file = new File(Constants.SCRATCH_FILES_DIRECTORY_RECORD, "clip." + (i + 1) + ".wav");
            if (!file.exists()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Clears the app state for recording if the user changes the example, language, or country.
     *
     * @param tabs The tabs to set not closable.
     */
    public static void setNotClosable(Tab... tabs) {
        for (Tab tab : tabs) {
            tab.setClosable(false);
        }
    }
}
