package io.github.cmuphil.earthlinguistfx.ui;

import io.github.cmuphil.earthlinguistfx.audio.AudioManager;
import io.github.cmuphil.earthlinguistfx.data.TableData;
import io.github.cmuphil.earthlinguistfx.data.TableRow;
import io.github.cmuphil.earthlinguistfx.examples.Examples;
import io.github.cmuphil.earthlinguistfx.utils.Constants;
import io.github.cmuphil.earthlinguistfx.utils.UiUtils;
import io.github.cmuphil.earthlinguistfx.utils.ZipUtils;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static io.github.cmuphil.earthlinguistfx.utils.Constants.*;
import static io.github.cmuphil.earthlinguistfx.utils.UiUtils.*;
import static io.github.cmuphil.earthlinguistfx.utils.ZipUtils.deleteDirectoryContents;


/**
 * An application that allows users to listen to publicly available clips and record their own clips for a given
 * example. The example is presented as a table, with images in the first column and checkmarks in the remaining
 * columns. The user is asked to record audio for each checkmark column simultaneously describing all the pictures in
 * the checkmark rows for that column. The user can then save their clips to their hard drive. The user can also listen
 * to clips from the public database. Also, the user can annotate their clips with their own annotations and expert
 * annotations.
 *
 * @author josephramsey
 * @author tomwerner
 */
public class EarthLinguist extends Application {

    /**
     * The app state for recording.
     */
    private AppState appStateRecord;
    /**
     * The app state for listening.
     */
    private AppState appStateListen;
    /**
     * Whether expert annotations are selected.
     */
    private boolean expertAnnotationsSelected = false;

    /**
     * The tab for listening to clips.
     */
    private Tab whichSample;

    /**
     * The tab for listening to clips.
     */
    private Tab listenToClips;

    /**
     * The tab for showing annotations of clips the user is listening to.
     */
    private Tab clipAnnotationsListen;

    /**
     * The tab choosing which example to record.
     */
    private Tab whichExample;

    /**
     * The tab for recording clips.
     */
    private Tab recordClips;

    /**
     * The tab for saving recorded clips.
     */
    private Tab saveClips;

    /**
     * The tab for annotating recorded clips.
     */
    private Tab annotateClipsRecord;

    /**
     * The file to save.
     */
    private File saveFile = null;

    /**
     * The tab for recording.
     */
    private Tab record;

    /**
     * The tab pane for recording.
     */
    private TabPane recordTabs;

    /**
     * The tab pane for the left side of the application.
     */
    private TabPane leftTabs;

    /**
     * The main method.
     *
     * @param args the command line arguments, ignored.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * The start method. This sets up user selection and makes a table.
     *
     * @param primaryStage the primary stage.
     */
    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(setUpUi(primaryStage), 800, 600);
        scene.getStylesheets().add("styles.css");
        primaryStage.setScene(scene);
        primaryStage.setTitle("EarthLinguist");
        primaryStage.show();
        primaryStage.toFront();
    }

    /**
     * Sets up the overall layout of the application interface. Organizes the configuration of tabs for the main
     * functionalities, such as Introduction, Listen to Clips and Record Clips. Returns the main element of the
     * interface.
     *
     * @param primaryStage the primary stage.
     * @return the main element of the interface.
     */
    private TabPane setUpUi(Stage primaryStage) {
        if (new File(Constants.PUBLIC_FILES_DIRECTORY).exists()) {
            deleteDirectoryContents(new File(Constants.PUBLIC_FILES_DIRECTORY));
            if (new File(Constants.PUBLIC_FILES_DIRECTORY).exists()) {
                if (!new File(Constants.PUBLIC_FILES_DIRECTORY).delete()) {
                    System.out.println("Error deleting public files directory.");
                }
            }
        }

        try {
            appStateRecord = AppState.fromJson(Constants.SCRATCH_FILES_DIRECTORY_RECORD);
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error loading app state from file: " + e.getMessage()).showAndWait();
            deleteDirectoryContents(new File(SCRATCH_FILES_DIRECTORY_LISTEN));
        }
        try {
            appStateListen = AppState.fromJson(SCRATCH_FILES_DIRECTORY_LISTEN);
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error loading app state from file: " + e.getMessage()).showAndWait();
            deleteDirectoryContents(new File(SCRATCH_FILES_DIRECTORY_RECORD));
        }

        ZipUtils.deleteDirectoryContents(new File(Constants.SCRATCH_ZIPS));
        appStateListen = null;

        leftTabs = new TabPane();
        leftTabs.setSide(Side.LEFT);

        // This should be empty on startup.
        if (new File(SCRATCH_ZIPS).exists()) {
            deleteDirectoryContents(new File(SCRATCH_ZIPS));
        }

        try {
            UiUtils.checkClipsDirectory(SCRATCH_FILES_DIRECTORY_LISTEN);
        } catch (Exception e) {
            System.out.println("Hidden state for listening to cips has been corrupted; deleting.");
            deleteDirectoryContents(new File(SCRATCH_FILES_DIRECTORY_LISTEN));
        }

        try {
            UiUtils.checkClipsDirectory(SCRATCH_FILES_DIRECTORY_RECORD);
        } catch (Exception e) {
            System.out.println("Hidden state for recording cips has been corrupted; deleting.");
            deleteDirectoryContents(new File(SCRATCH_FILES_DIRECTORY_RECORD));
        }

        Tab introduction = new Tab("Introduction");
        Tab listen = new Tab("Listen");
        record = new Tab("Record");

        introduction.setClosable(false);
        listen.setClosable(false);
        record.setClosable(false);

        introduction.setContent(getIntroContent());

        leftTabs.getTabs().add(introduction);
        leftTabs.getTabs().add(listen);
        leftTabs.getTabs().add(record);

        // Set up the tabs for recording.
        whichExample = new Tab("Select an Example to Record", getWhichExampleTabContent(primaryStage));
        recordClips = new Tab("Record Your Clips", getRecordClipsTabContent());
        annotateClipsRecord = new Tab("Annotate Your Clips", getAnnotationTabContentRecord());
        saveClips = new Tab("Save Your Clips", getSaveClipsTabContent(primaryStage));

        setNotClosable(whichExample, recordClips, annotateClipsRecord, saveClips);

        recordTabs = new TabPane(whichExample, recordClips, annotateClipsRecord, saveClips);
        recordTabs.setSide(Side.TOP);
        record.setContent(recordTabs);

        recordTabs.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == whichExample) {
                whichSample.setContent(getWhichSampleTabContent(primaryStage));
            } else if (newTab == recordClips) {
                recordClips.setContent(getRecordClipsTabContent());
            } else if (newTab == annotateClipsRecord) {
                annotateClipsRecord.setContent(getAnnotationTabContentRecord());
            } else if (newTab == saveClips) {
                saveClips.setContent(getSaveClipsTabContent(primaryStage));
            }
        });

        // Set up the tabs for listening.
        whichSample = new Tab("Load Clips", getWhichSampleTabContent(primaryStage));
        listenToClips = new Tab("Listen to Clips", getListenToClipsTabContent());
        clipAnnotationsListen = new Tab("Read Annotations", getAnnotationTabContentListen());

        setNotClosable(whichSample, listenToClips, clipAnnotationsListen);

        TabPane listenTabs = new TabPane(whichSample, listenToClips, clipAnnotationsListen);
        listenTabs.setSide(Side.TOP);
        listen.setContent(listenTabs);

        listenTabs.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == whichSample) {
                whichSample.setContent(getWhichSampleTabContent(primaryStage));
            } else if (newTab == listenToClips) {
                listenToClips.setContent(getListenToClipsTabContent());
            } else if (newTab == clipAnnotationsListen) {
                clipAnnotationsListen.setContent(getAnnotationTabContentListen());
            }
        });

        return leftTabs;
    }

    /**
     * Sets up the content for the Introduction Tab. It includes details about the application and an image. Returns the
     * Node containing this content.
     *
     * @return the Node containing this content.
     */
    private Node getIntroContent() {
        ImageView imageView;

        TableData tableData = Examples.getInstance().getExample(1);
        String path = tableData.getRow(0).getExampleImage();
        try (InputStream resourceStream = EarthLinguist.class.getResourceAsStream(path)) {
            if (resourceStream == null) {
                throw new RuntimeException("Couldn't load image: " + path);
            }

            imageView = new ImageView(new Image(resourceStream));
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(200.0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new VBox(getLabel(""), UiUtils.getCenteredImage(imageView),
                getLabel("Welcome to the EarthLinguist app!"),
                getLabel("Our goal is to build a library of sounds clips recorded by users suitable for "
                        + "listening or doing linguistics research. The sound clips are recorded in various languages "
                        + "to describe pictures in a table, in a structured way. If you know the classic "
                        + "Gavagai experiment, we're aiming to implement that idea. We don't tell you how to describe "
                        + "the pictures; rather, you describe them in your own language, and others can listen to "
                        + "how you describe them and try to figure out features of your language."),
                getLabel("Your recordings can be very useful to linguists who are trying to describe features of "
                        + "pronunciation, word usage, and phraseology in your language, so this is for a good cause! So "
                        + "THANK YOU in advance for participating!"));
    }

    /**
     * Sets up the content for the "Which Sample?" tab, preparing combo boxes, for example, language, country, and
     * sample selection. Returns the Node containing this content. For the Listen tab.
     *
     * @return the Node containing this content.
     */
    private Node getWhichSampleTabContent(Stage primaryStage) {
        Button loadSample = getLoadButton(primaryStage);
        VBox vBox = new VBox(
                getLabeledRegion("To load a sample recording from your hard drive to " +
                        "edit or listen, select it here:", loadSample));
        vBox.setPadding(new Insets(10));

        Button edit = new Button("Edit");

        edit.setOnAction(e -> {
            if (appStateListen != null) {
                try {
                    ZipUtils.deleteDirectoryContents(new File(SCRATCH_ZIPS));

                    if (!new File(SCRATCH_ZIPS).exists()) {
                       if (!new File(SCRATCH_ZIPS).mkdirs()) {
                            throw new RuntimeException("Error creating scratch zips directory.");
                       }
                    } else if (!new File(SCRATCH_ZIPS).isDirectory()) {
                        throw new RuntimeException("Scratch zips is not a directory.");
                    }

                    ZipUtils.zip(new File(SCRATCH_FILES_DIRECTORY_LISTEN), new File(SCRATCH_ZIPS, "loaded.zip"));
                    ZipUtils.deleteDirectoryContents(new File(SCRATCH_FILES_DIRECTORY_RECORD));
                    ZipUtils.unzip(new File(SCRATCH_ZIPS, "loaded.zip"), new File(SCRATCH_FILES_DIRECTORY_RECORD));

                    appStateRecord = AppState.fromJson(SCRATCH_FILES_DIRECTORY_LISTEN);
                    whichExample.setContent(getWhichExampleTabContent(primaryStage));
                    recordClips.setContent(getRecordClipsTabContent());
                    annotateClipsRecord.setContent(getAnnotationTabContentRecord());
                    saveClips.setContent(getSaveClipsTabContent(primaryStage));
                    leftTabs.getSelectionModel().select(record);
                    recordTabs.getSelectionModel().select(whichExample);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        if (appStateListen != null) {
            String selectedCountry = appStateListen.getSelectedCountry();

            if (selectedCountry != null && !selectedCountry.isBlank()) {
                vBox.getChildren().addAll(
                        getLabel("Wonderful, you've loaded a sample to listen to!"),
                        getLabel("Here is some information about the sample you've loaded:"),
                        getLabel("\tThe language of the sample is : " + appStateListen.getSelectedLanguage()),
                        getLabel("\tThe country of origin of the sample is : " + appStateListen.getSelectedCountry()),
                        getLabel("\tThe region(s) listed are: " + appStateListen.getEnteredRegion()),
                        getLabel("\tThe index of the sample is : " + appStateListen.getSelectedIndex()),
                        getLabeledRegion("To edit this sample and save out a revised sample, click this button:", edit)
                );
            }
        }

        return vBox;
    }

    /**
     * Return the content for the Annotations tab in the Listen tab.
     *
     * @return the content for the Annotations tab in the Listen tab.
     */
    private ScrollPane getAnnotationTabContentListen() {
        if (appStateListen == null) {
            return new ScrollPane();
        }

        VBox vbox = new VBox();

        TableView<TableRow> table = UiUtils.getTable(true, appStateListen.getSelectedIndex(), appStateListen, SCRATCH_FILES_DIRECTORY_LISTEN);
        int numColumns = table.getColumns().size() - 1;

//        if (appStateListen.getUserAnnotations().length != numColumns) {
//            new Alert(Alert.AlertType.ERROR, "Error: Number of columns in app state does not match the number of user annotations " +
//                                             "given. The number of columns is listed as " + numColumns + " but there are " +
//                                             appStateListen.getUserAnnotations().length + " annotations in the appState.json file.").showAndWait();
//            return new ScrollPane();
//        }

        CheckBox checkBox = getExpertAnnotationsCheckBox(vbox);

        checkBox.setOnAction(e -> {
            expertAnnotationsSelected = checkBox.isSelected();
            clipAnnotationsListen.setContent(getAnnotationTabContentListen());
        });

        for (int i = 0; i < numColumns; i++) {
            File audioFileDir = new File(SCRATCH_FILES_DIRECTORY_LISTEN);

            try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(UiUtils.getSoundFile(audioFileDir, i))) {
                TextArea userArea = UiUtils.getUserArea(i, true, appStateListen, SCRATCH_FILES_DIRECTORY_LISTEN, numColumns);
                TextArea expertArea = UiUtils.getExpertArea(i, true, appStateListen, SCRATCH_FILES_DIRECTORY_LISTEN, numColumns);

                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);

                if (appStateListen.getUserAnnotations()[i].isBlank()) {
                    userArea.setPromptText("User annotation: No annotation was provided.");
                } else {
                    userArea.setPromptText("User annotation");
                }

                if (appStateListen.getUserAnnotations()[i].isBlank()) {
                    expertArea.setPromptText("Expert annotation: No annotation was provided.");
                } else {
                    expertArea.setPromptText("Expert annotation");
                }

                expertArea.setEditable(false);
                expertArea.getStyleClass().add("text-userArea-disabled");

                setUpAreas(i, audioFileDir, vbox, userArea, checkBox, expertArea);
            } catch (Exception ex) {
                TextArea userArea = UiUtils.getUserArea(i, false, appStateListen, SCRATCH_FILES_DIRECTORY_LISTEN, numColumns);
                TextArea expertArea = UiUtils.getExpertArea(i, false, appStateListen, SCRATCH_FILES_DIRECTORY_LISTEN, numColumns);

                userArea.setPromptText("User annotation: This clip was not recorded by the user.");
                userArea.setText("");
                appStateListen.getUserAnnotations()[i] = "";
                userArea.getStyleClass().add("text-userArea-disabled");

                expertArea.setPromptText("Expert annotation: This clip was not recorded by the user.");
                expertArea.setText("");
                appStateListen.getExpertAnnotations()[i] = "";

                vbox.getChildren().add(new Label("Column " + (i + 1) + " (Not recorded)" + ":"));
                vbox.getChildren().add(userArea);

                if (checkBox.isSelected()) {
                    vbox.getChildren().add(expertArea);
                }

            }

            vbox.getChildren().add(new Label(""));
        }

        vbox.setPadding(new Insets(10));
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(vbox);
        return scrollPane;
    }

    private void setUpAreas(int i, File audioFileDir, VBox vbox, TextArea userArea, CheckBox checkBox, TextArea expertArea) {
        BooleanProperty recordProperty = new SimpleBooleanProperty(false);

        HBox playBox = AudioManager.getInstance().createControlButtons(recordProperty, i, audioFileDir,
                false, appStateRecord);
        vbox.getChildren().add(new Label("Column " + (i + 1) + ":"));
        vbox.getChildren().add(new HBox(playBox, userArea));

        if (checkBox.isSelected()) {
            vbox.getChildren().add(expertArea);
        }
    }

    private CheckBox getExpertAnnotationsCheckBox(VBox vbox) {
        vbox.getChildren().add(new Label("In your own language, please write out what you said for each column:"));

        CheckBox checkBox = new CheckBox("Show expert annotations");
        checkBox.setSelected(expertAnnotationsSelected);

        vbox.getChildren().add(new Label(""));
        vbox.getChildren().add(checkBox);
        vbox.getChildren().add(new Label(""));
        return checkBox;
    }


    /**
     * Returns the content for the "Listen to Clips" tab.
     *
     * @return the content for the "Listen to Clips" tab.
     */
    private TableView<TableRow> getListenToClipsTabContent() {
        if (this.appStateListen == null) {
            return new TableView<>();
        }

        return UiUtils.getTable(false, appStateListen.getSelectedIndex(),
                appStateListen, SCRATCH_FILES_DIRECTORY_LISTEN);
    }

    /**
     * Returns the content for the "Which Example?" tab for the record tab.
     *
     * @return the content for the "Which Example?" tab for the record tab.
     */
    private Node getWhichExampleTabContent(Stage primaryStage) {
        comboBoxes result = UiUtils.getSetUpComboBoxes();

        TextArea regionArea = new TextArea();
        regionArea.setPrefColumnCount(60);
        regionArea.setPrefRowCount(2);
        regionArea.setPromptText("Enter a city/state/region (or a list of such regions)");

        // Initialize stuff
        if (result.examplesCombo.getItems().contains(appStateRecord.getSelectedIndex())) {
            result.examplesCombo.setValue(appStateRecord.getSelectedIndex());
        }

        if (result.languagesCombo.getItems().contains(appStateRecord.getSelectedLanguage())) {
            result.languagesCombo.setValue(appStateRecord.getSelectedLanguage());
        }

        if (result.countriesCombo.getItems().contains(appStateRecord.getSelectedCountry())) {
            result.countriesCombo.setValue(appStateRecord.getSelectedCountry());
        }

        if (appStateRecord.getEnteredRegion() != null) {
            regionArea.setText(appStateRecord.getEnteredRegion());
        }

        // Set up listeners
        result.examplesCombo.setOnAction(e ->
                clearForRecord(result.examplesCombo, result.languagesCombo, result.countriesCombo, regionArea));

        result.languagesCombo.setOnAction(e ->
                clearForRecord(result.examplesCombo, result.languagesCombo, result.countriesCombo, regionArea)
        );

        result.countriesCombo.setOnAction(e ->
                clearForRecord(result.examplesCombo, result.languagesCombo, result.countriesCombo, regionArea)
        );

        regionArea.textProperty().addListener((observable, oldValue, newValue) ->
                clearForRecord(result.examplesCombo, result.languagesCombo, result.countriesCombo, regionArea));

        Button load = getLoadExamplesButton(primaryStage);
        Button stored = getRestoreExamplesButton(primaryStage);

        VBox vBox = new VBox(
                getLabeledRegion("For which example table would you like to record clips?", result.examplesCombo, stored, load),
                getLabeledRegion("In which language would you like to record the clips?", result.languagesCombo),
                getLabel("Please answer the following questions to help us understand your background:"),
                getLabel("(1) In which country did you mainly grow up?"), UiUtils.getRegion(result.countriesCombo),
                getLabel("(2) In which city/town/region in this country did you grow up? (If helpful, please give a list of regions.)"),
                UiUtils.getRegion(regionArea));
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(10));
        return vBox;
    }

    private Button getRestoreExamplesButton(Stage primaryStage) {
        Button stored = new Button("Restore");

        stored.setOnAction(actionEvent -> {
            try {
                Examples.getInstance().loadExamplesFromJar();
            } catch (IOException | IllegalArgumentException e) {
                new Alert(Alert.AlertType.ERROR, "Error loading examples from jar: " + e.getMessage()).showAndWait();
            }

            whichExample.setContent(getWhichExampleTabContent(primaryStage));
            ZipUtils.deleteDirectoryContents(new File(LOADED_EXAMPLES));
        });
        return stored;
    }

    private Button getLoadExamplesButton(Stage primaryStage) {
        Button load = new Button("Load New Examples...");

        load.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            fileChooser.setTitle("Open File");

            // Set extension filter, if you want to restrict the file type
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("ZIP files (*.zip)", "*.zip");
            fileChooser.getExtensionFilters().add(extFilter);

            File file = fileChooser.showOpenDialog(primaryStage);

            try {
                Examples.getInstance().loadExamplesFromZip(file);
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Couldn't load that file: " + e.getMessage()).showAndWait();
                return;
            }

            whichExample.setContent(getWhichExampleTabContent(primaryStage));
        });
        return load;
    }

    /**
     * Returns the content for the "Record Clips" tab.
     *
     * @return the content for the "Record Clips" tab.
     */
    private TableView<TableRow> getRecordClipsTabContent() {
        int selectedIndex = appStateRecord.getSelectedIndex();
        TableView<TableRow> table = UiUtils.getTable(true, selectedIndex, appStateRecord, Constants.SCRATCH_FILES_DIRECTORY_RECORD);
        appStateRecord.setNumColumns(table.getColumns().size() - 1);
        return table;
    }

    /**
     * Returns the content for the Annotation tab for the Record tab.
     *
     * @return the content for the Annotation tab for the Record tab.
     */
    private ScrollPane getAnnotationTabContentRecord() {
        VBox vbox = new VBox();

        TableView<TableRow> table = UiUtils.getTable(true, appStateRecord.getSelectedIndex(), appStateRecord, Constants.SCRATCH_FILES_DIRECTORY_RECORD);
        int numColumns = table.getColumns().size() - 1;

        if (appStateRecord.getUserAnnotations().length != numColumns) {
            appStateRecord.setNumColumns(numColumns);
        }

        CheckBox checkBox = getExpertAnnotationsCheckBox(vbox);

        checkBox.setOnAction(e -> {
            expertAnnotationsSelected = checkBox.isSelected();
            annotateClipsRecord.setContent(getAnnotationTabContentRecord());
        });

        for (int i = 0; i < numColumns; i++) {
            File audioFileDir = new File(Constants.SCRATCH_FILES_DIRECTORY_RECORD);

            try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(UiUtils.getSoundFile(audioFileDir, i))) {
                TextArea userArea = UiUtils.getUserArea(i, true, appStateRecord, Constants.SCRATCH_FILES_DIRECTORY_RECORD, numColumns);
                TextArea expertArea = UiUtils.getExpertArea(i, true, appStateRecord, Constants.SCRATCH_FILES_DIRECTORY_RECORD, numColumns);

                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);

                setUpAreas(i, audioFileDir, vbox, userArea, checkBox, expertArea);

                vbox.getChildren().add(new Label(""));
            } catch (Exception ex) {
                TextArea userArea = UiUtils.getUserArea(i, false, appStateRecord, Constants.SCRATCH_FILES_DIRECTORY_RECORD, numColumns);
                TextArea expertArea = UiUtils.getExpertArea(i, false, appStateRecord, Constants.SCRATCH_FILES_DIRECTORY_RECORD, numColumns);

                userArea.setPromptText("User annotation: This clip was not recorded by the user.");
                userArea.setText("");
                appStateRecord.getUserAnnotations()[i] = "";
                userArea.setEditable(false);
                userArea.getStyleClass().add("text-userArea-disabled");

                expertArea.setPromptText("Expert annotation: This clip was not recorded by the user.");
                expertArea.setText("");
                appStateRecord.getExpertAnnotations()[i] = "";
                expertArea.setEditable(false);
                expertArea.getStyleClass().add("text-userArea-disabled");

                vbox.getChildren().add(new Label("Column " + (i + 1) + (" (Not recorded)") + ":"));
                vbox.getChildren().add(userArea);

                if (checkBox.isSelected()) {
                    vbox.getChildren().add(expertArea);
                }

                vbox.getChildren().add(new Label(""));
            }
        }

        vbox.setPadding(new Insets(10));
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(vbox);
        return scrollPane;
    }

    /**
     * Returns the content for the "Save Clips" tab.
     *
     * @param primaryStage the primary stage.
     * @return the content for the "Save Clips" tab.
     */
    private Pane getSaveClipsTabContent(Stage primaryStage) {
        Button save = getSaveAsButton(primaryStage);

        VBox vBox = new VBox(getLabel("You can save your clips and annotations here to your hard drive. You can then " +
                "view them the Listen tab."),
                getLabel("Please only save recordings once everything is the way you want it."),
                UiUtils.getRegion(save)
        );

        if (saveFile != null) {
            vBox.getChildren().add(getLabel("Great! Your clips have been saved as " + saveFile.getName()));
        }

        vBox.setSpacing(10);
        vBox.setPadding(new Insets(10));

        return vBox;
    }

    private Button getSaveAsButton(Stage primaryStage) {
        Button save = new Button("Save As...");

        save.setOnAction(e -> {
            File saveFile = ZipUtils.save(appStateRecord.getSelectedIndex(),
                    appStateRecord.getSelectedLanguage(), appStateRecord.getSelectedCountry(),
                    primaryStage);

            if (saveFile != null) {
                ZipUtils.nextZipName(saveFile.getParent(), saveFile.getName());
                this.saveFile = saveFile;
                saveClips.setContent(getSaveClipsTabContent(primaryStage));
            } else {
                throw new RuntimeException("Error saving file");
            }
        });
        return save;
    }

    /**
     * Constructs a button for loading a file.
     *
     * @param primaryStage the primary stage.
     */
    private Button getLoadButton(Stage primaryStage) {
        Button load = new Button("Load Zip...");

        load.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open File");

            // Set extension filter, if you want to restrict the file type
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("ZIP files (*.zip)", "*.zip");
            fileChooser.getExtensionFilters().add(extFilter);

            File file = fileChooser.showOpenDialog(primaryStage);

            AppState load1;

            try {
                load1 = UiUtils.loadAppState(file, SCRATCH_FILES_DIRECTORY_LISTEN);
            } catch (Exception e) {
                ZipUtils.deleteDirectoryContents(new File(SCRATCH_FILES_DIRECTORY_LISTEN));
                new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
                throw new RuntimeException(e);
            }

            appStateListen = load1;
            whichSample.setContent(getWhichSampleTabContent(primaryStage));
        });

        return load;
    }

    /**
     * Clears the appState for recording.
     *
     * @param examplesCombo  the examples combo box.
     * @param languagesCombo the languages combo box.
     * @param countriesCombo the countries combo box.
     * @param regionArea     the region area.
     */
    private void clearForRecord(ComboBox<Integer> examplesCombo, ComboBox<String> languagesCombo,
                                ComboBox<String> countriesCombo, TextArea regionArea) {
        appStateRecord.setSelectedIndex(examplesCombo.getValue());
        appStateRecord.setSelectedLanguage(languagesCombo.getValue());
        appStateRecord.setSelectedCountry(countriesCombo.getValue());
        appStateRecord.setEnteredRegion(regionArea.getText());
        try {
            AppState.toJson(appStateRecord, Constants.SCRATCH_FILES_DIRECTORY_RECORD);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Error saving app state to file: " + e.getMessage()).showAndWait();
            throw new RuntimeException(e);
        }
        deleteDirectoryContents(new File(Constants.SCRATCH_FILES_DIRECTORY_RECORD));
    }

    /**
     * A record for the combo boxes.
     */
    public record comboBoxes(ComboBox<Integer> examplesCombo, ComboBox<String> countriesCombo,
                             ComboBox<String> languagesCombo) {
    }
}
