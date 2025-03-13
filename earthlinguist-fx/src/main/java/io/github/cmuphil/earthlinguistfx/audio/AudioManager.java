package io.github.cmuphil.earthlinguistfx.audio;

import io.github.cmuphil.earthlinguistfx.ui.AppState;
import io.github.cmuphil.earthlinguistfx.utils.UiUtils;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * A class for managing the microphone. Methods for recording, playing back, and stopping the microphone. Also records
 * and plays back audio for a given checkmark column, saving this for now to files per column.
 *
 * @author josephramsey
 */
public class AudioManager {

    /**
     * Singleton pattern.
     */
    private static final AudioManager instance = new AudioManager();

    /**
     * The microphone line
     */
    private TargetDataLine microphone;

    /**
     * Constructs the singleton instance.
     */
    private AudioManager() {

    }

    /**
     * Singleton pattern.
     *
     * @return the singleton instance.
     */
    public static AudioManager getInstance() {
        return instance;
    }

    /**
     * Checks the audio file directory.
     *
     * @param audioFileDir the audio file directory.
     * @throws IOException if the audio file directory could not be created.
     */
    private static void checkAudioFileDir(File audioFileDir) throws IOException {
        if (audioFileDir == null) {
            throw new IOException("The audio file directory is null. This is a problem.");
        }

        if (!audioFileDir.exists()) {
            if (!audioFileDir.mkdirs()) {
                throw new IOException("Could not create the audio file directory: " + audioFileDir);
            }
        }
    }

    /**
     * Creates the record and play buttons for a given checkmark column.
     *
     * @param isRecording  the boolean property for recording.
     * @param column       the column number.
     * @param audioFileDir the audio file directory.
     * @param record       true if recording is allowed.
     * @param appState     the app state.
     * @return the HBox containing the buttons.
     */
    public HBox createControlButtons(BooleanProperty isRecording, int column, File audioFileDir,
                                     boolean record, AppState appState) {

        Button recordButton = new Button("Record");

        if (UiUtils.getSoundFile(audioFileDir, column).exists()) {
            recordButton.setStyle("-fx-background-color: #98FF98;");
            recordButton.setStyle("-fx-fill-color: #98FF98;");
        }

        if (record) {
            recordButton.setOnAction(e -> {
                if (isRecording.get()) {
                    stopRecording();
                    isRecording.set(false);
                    recordButton.setText("Record");
                } else {
                    boolean ok;
                    try {
                        captureAudio(column, audioFileDir, appState);
                        ok = true;
                    } catch (IOException ex) {
                        ok = false;
                    }

                    if (!ok) {
                        isRecording.set(false);
                        recordButton.setText("Record");
                    } else {
                        isRecording.set(true);
                        recordButton.setText("Stop");
                    }

                    recordButton.setStyle("-fx-background-color: #98FF98;"); //??
                    recordButton.setStyle("-fx-fill-color: #98FF98;");
                }
            });
        }

        Button playButton = new Button("Play");
        playButton.setOnAction(e -> {
            recordButton.setText("Record");
            stopRecording();
            playRecording(column, audioFileDir);
        });

        HBox hBox;

        if (record) {
            hBox = new HBox(5, recordButton, playButton);
        } else {
            hBox = new HBox(5, playButton);
        }

        hBox.setAlignment(Pos.CENTER);
        return hBox;
    }

    /**
     * Captures audio from the microphone and saves it to a file.
     *
     * @param column       the column number.
     * @param audioFileDir the audio file directory.
     * @param appState     the app state.
     * @throws IOException if the audio file directory could not be created.
     */
    public void captureAudio(int column, File audioFileDir, AppState appState) throws IOException {
        try {
            checkAudioFileDir(audioFileDir);
        } catch (IOException e) {
            throw new IOException(e);
        }

        try {
            stopRecording();
            AudioFormat format = new AudioFormat(48000.0f, 16, 1, true,
                    true);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("There was a problem accessing your microphone.");
            }

            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            Thread targetThread = new Thread(() -> {
                AudioInputStream audioStream = new AudioInputStream(microphone);
                try {
                    AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, UiUtils.getSoundFile(audioFileDir, column));
                    AppState.toJson(appState, audioFileDir.toString());
                } catch (IOException e) {
                    System.out.println("There was a problem saving the recording to a file. " +
                            "Please try again. " + "\n" + e.getMessage());
                }
            });
            targetThread.start();
        } catch (Exception ex) {
            System.out.println("There was a problem accessing the microphone. " +
                    "Please check your microphone settings. " + "\n" + ex.getMessage());
        }
    }

    /**
     * Plays back the audio for a given checkmark column.
     *
     * @param column       the column number.
     * @param audioFileDir the audio file directory.
     */
    public void playRecording(int column, File audioFileDir) {
        try {
            checkAudioFileDir(audioFileDir);
        } catch (IOException e) {
            return;
        }

        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(
                UiUtils.getSoundFile(audioFileDir, column))) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
        } catch (Exception ex) {
            System.out.println("There was a problem playing back the audio. Perhaps it was not recorded yet. "
                    + "\n" + ex.getMessage());
        }
    }

    /**
     * Stops the microphone and closes it.
     */
    public void stopRecording() {
        if (microphone != null) {
            microphone.stop();
            microphone.close();
        }
    }
}
