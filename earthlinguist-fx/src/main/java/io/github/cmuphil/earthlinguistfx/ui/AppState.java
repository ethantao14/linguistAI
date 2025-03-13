package io.github.cmuphil.earthlinguistfx.ui;

import com.google.gson.Gson;
import io.github.cmuphil.earthlinguistfx.utils.UiUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents the state of the application. There is a separate state for listening and recording.
 *
 * @author josephramsey
 */
public class AppState {

    /**
     * The name of the selected example.
     */
    private final String selectedExampleName = "";

    /**
     * The index of the selected example.
     */
    private int selectedIndex = 1;

    /**
     * The selected language.
     */
    private String selectedLanguage = "";

    /**
     * The selected country.
     */
    private String selectedCountry = "";

    /**
     * The selected region.
     */
    private String enteredRegion = "";

    /**
     * The index of the selected sample.
     */
    private int selectedSampleIndex = 1;

    /**
     * The time stamp.
     */
    private String timeStamp = null;

    /**
     * The user annotations.
     */
    private String[] userAnnotations = new String[0];

    /**
     * The expert annotations.
     */
    private String[] expertAnnotations = new String[0];

    /**
     * The number of columns.
     */
    private int numColumns = 0;

    public AppState() {
        setNumColumns(0);
    }

    /**
     * Loads an AppState from a 'state.json' file located in the specified directory, or creates a new AppState if the
     * directory does not exist or the 'state.json' file is not found within it. If the directory or file does not
     * exist, this method will create the directory, generate a new AppState, save it to 'state.json', and then return
     * it. If a 'state.json' file exists, the method attempts to load the AppState from it, applying special handling if
     * the loaded AppState's selected example name is "Chair Example" by setting its selected index to 1. If any error
     * occurs during the loading process, a RuntimeException is thrown.
     *
     * @param dir the directory where 'state.json' is located or will be saved.
     * @return the loaded or newly created AppState.
     * @throws IOException if an error occurs during file reading.
     */
    public static AppState fromJson(String dir) throws IOException {
        AppState appState = new AppState();

        if (!new File(dir).exists()) {
            if (!new File(dir).mkdirs()) {
                throw new IOException("Error creating directory: " + dir);
            }
        }

        if (!new File(dir, "state.json").exists()) {
            toJson(appState, dir);
            return appState;
        }

        Gson gson = new Gson();

        AppState appState1;

        try (FileReader reader = new FileReader(new File(dir, "state.json"))) {
            appState1 = gson.fromJson(reader, AppState.class);

            if ("Chair Example".equals(appState1.getSelectedExampleName())) {
                appState1.setSelectedIndex(1);
            }

            String[] userAnnotations = appState1.getUserAnnotations();
            String[] expertAnnotations = appState1.getExpertAnnotations();

            // In case annotations are missing.
            if (userAnnotations.length < appState1.numColumns) {
                String[] userAnnotations2 = new String[appState1.numColumns];
                Arrays.fill(userAnnotations2, "Missing");
                System.arraycopy(userAnnotations, 0, userAnnotations2, 0, userAnnotations.length);
                appState1.setUserAnnotations(userAnnotations2);
            }

            if (expertAnnotations.length < appState1.numColumns) {
                String[] expertAnnotations2 = new String[appState1.numColumns];
                Arrays.fill(expertAnnotations2, "Missing");
                System.arraycopy(expertAnnotations, 0, expertAnnotations2, 0, userAnnotations.length);
                appState1.setUserAnnotations(expertAnnotations2);
            }

            return appState1;
        } catch (Exception e) {
            throw new IllegalStateException("Error reading file: " + e.getMessage(), e);
        }
    }

    /**
     * Saves the AppState object to a file named 'state.json' in the specified directory. If the directory does not
     * exist, attempts to create it. Displays an error alert if the directory cannot be created. The AppState's
     * timestamp is updated to the current date and time before serialization. Throws a RuntimeException if an error
     * occurs during file writing.
     *
     * @param appState the AppState object to be saved.
     * @param dir      the directory where the 'state.json' file will be saved. If the directory does not exist, it will
     *                 be created.
     * @throws IOException if an error occurs during file writing.
     */
    public static void toJson(AppState appState, String dir) throws IOException {
        if (!new File(dir).exists()) {
            if (!new File(dir).mkdirs()) {
                if (!new File(dir).mkdirs()) {
                    throw new IOException("Error creating directory: " + dir);
                }
            }
        }

        try (FileWriter writer = new FileWriter(new File(dir, "state.json"), StandardCharsets.UTF_8)) {
            Gson gson = new Gson();
            appState.setTimeStamp(UiUtils.formatCurrentDateTime());
            String json = gson.toJson(appState);
            writer.write(json);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Returns the app state from the given directory.
     *
     * @return the app state from the given directory.
     */
    public String getSelectedExampleName() {
        return selectedExampleName;
    }

    /**
     * Returns the index of the selected example.
     *
     * @return the index of the selected example.
     */
    public Integer getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * Sets the index of the selected example.
     *
     * @param selectedIndex the index of the selected example. Must be >= 1.
     */
    public void setSelectedIndex(int selectedIndex) {
        if (selectedIndex < 1) {
            throw new RuntimeException("Invalid selected index: " + selectedIndex);
        }

        this.selectedIndex = selectedIndex;
    }

    /**
     * Sets the number of columns. This method is used to initialize the user and expert annotations.
     *
     * @param numColumns the number of columns.
     */
    public void setNumColumns(int numColumns) {
        if (numColumns < 0) {
            numColumns = 0;
        }

        if (numColumns == userAnnotations.length && numColumns == expertAnnotations.length) {
            return;
        }

        this.numColumns = numColumns;
        this.userAnnotations = new String[numColumns];
        this.expertAnnotations = new String[numColumns];

        for (int i = 0; i < numColumns; i++) {
            this.userAnnotations[i] = "";
            this.expertAnnotations[i] = "";
        }
    }

    /**
     * Returns the user annotations.
     *
     * @return the user annotations.
     */
    public String[] getUserAnnotations() {
        return userAnnotations;
    }

    /**
     * Sets the user annotations. Don't delete; GSON needs this method.
     *
     * @param userAnnotations the user annotations.
     */
    public void setUserAnnotations(String[] userAnnotations) {
        if (userAnnotations.length != numColumns) {
            setNumColumns(userAnnotations.length);
        }

        this.userAnnotations = userAnnotations;
    }

    /**
     * Returns the expert annotations.
     *
     * @return the expert annotations.
     */
    public String[] getExpertAnnotations() {
        return expertAnnotations;
    }

    /**
     * Sets the expert annotations.
     *
     * @param expertAnnotations the expert annotations. Don't delete; GSON needs this method.
     */
    public void setExpertAnnotations(String[] expertAnnotations) {
        if (expertAnnotations.length != numColumns) {
            setNumColumns(expertAnnotations.length);
        }

        this.expertAnnotations = expertAnnotations;
    }

    /**
     * Returns the selected sample index. Note that the selected sample index is 1-based. Also, this index needs to be
     * kept in sync with the index of the selected example's file name; this is set when the file is loaded out or saved
     * in.
     *
     * @return the selected sample index.
     */
    public int getSelectedSampleIndex() {
        return selectedSampleIndex;
    }

    /**
     * Sets the selected sample index. Note that the selected sample index is 1-based. Also, this index needs to be kept
     * in sync with the index of the selected example's file name; this is set when the file is loaded out or saved in.
     *
     * @param selectedSampleIndex the selected sample index. Can be null.
     */
    public void setSelectedSampleIndex(int selectedSampleIndex) {
        if (selectedSampleIndex < 1) {
            throw new RuntimeException("Invalid selected sample index: " + selectedSampleIndex);
        }

        this.selectedSampleIndex = selectedSampleIndex;
    }

    /**
     * Returns the selected language. Possible values are in the file languages.txt in the resource directory.
     *
     * @return the selected language.
     */
    public String getSelectedLanguage() {
        return selectedLanguage;
    }

    /**
     * Sets the selected language. Possible values are in the file languages.txt in the resource directory.
     *
     * @param selectedLanguage the selected language.
     */
    public void setSelectedLanguage(String selectedLanguage) {
        this.selectedLanguage = Objects.requireNonNullElse(selectedLanguage, "");
    }

    /**
     * Returns the selected country. Possible values are in the file countries.txt in the resource directory.
     *
     * @return the selected country.
     */
    public String getSelectedCountry() {
        return selectedCountry;
    }

    /**
     * Sets the selected country. Possible values are in the file countries.txt in the resource directory.
     *
     * @param selectedCountry the selected country.
     */
    public void setSelectedCountry(String selectedCountry) {
        this.selectedCountry = Objects.requireNonNullElse(selectedCountry, "");
    }

    /**
     * Returns the entered region. This is a free-form text area.
     *
     * @return the entered region.
     */
    public String getEnteredRegion() {
        return enteredRegion;
    }

    /**
     * Sets the entered region. This is a free-form text area.
     *
     * @param enteredRegion the entered region.
     */
    public void setEnteredRegion(String enteredRegion) {
        this.enteredRegion = Objects.requireNonNullElse(enteredRegion, "");
    }

    /**
     * Returns the time stamp. Don't delete; GSON needs this method.
     *
     * @return the time stamp.
     */
    public String getTimeStamp() {
        return timeStamp;
    }

    /**
     * Sets the time stamp.
     *
     * @param timeStamp the time stamp.
     */
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = Objects.requireNonNullElse(timeStamp, "");
    }

    /**
     * Returns the string representation of the app state.
     *
     * @return the string representation of the app state.
     */
    public String toString() {
        StringBuilder s = new StringBuilder("AppState: " + "Example #" + selectedIndex + ", " + selectedLanguage + ", " + selectedCountry + ", " + enteredRegion);

        for (String userAnnotation : userAnnotations) {
            s.append(", ").append(userAnnotation);
        }

        for (String expertAnnotation : expertAnnotations) {
            s.append(", ").append(expertAnnotation);
        }

        s.append(", ").append(timeStamp);
        return s.toString();
    }
}
