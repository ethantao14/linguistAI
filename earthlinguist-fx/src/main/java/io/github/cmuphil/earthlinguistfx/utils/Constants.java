package io.github.cmuphil.earthlinguistfx.utils;

import java.time.format.DateTimeFormatter;

/**
 * Some constants for the EarthLinguist UI.
 *
 * @author josephramsey
 */
public class Constants {

    /**
     * The date time formatter.
     */
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * The directory where the app will store scratch files for recording.
     */
    public static final String SCRATCH_FILES_DIRECTORY_RECORD = System.getProperty("user.home") + "/.earthlinguist/scratch_record";

    /**
     * The directory where the app will store scratch files for listening.
     */
    public static final String SCRATCH_FILES_DIRECTORY_LISTEN = System.getProperty("user.home") + "/.earthlinguist/scratch_listen";

    /**
     * The directory where the app will store public sound zip files--those that are shared with the community. This
     * will be replaced with a database in the future.
     */
    public static final String PUBLIC_FILES_DIRECTORY = System.getProperty("user.home") + "/.earthlinguist/public_sound_zips";

    /**
     * The path to the directory where the examples that the user explicitly loads using the load examples button
     * are stored. This directory should be emptied when the application is opened and when the stored state is reverted.
     *
     * @value The user's home directory + "/.earthlinguist/loaded_examples"
     */
    public static final String LOADED_EXAMPLES = System.getProperty("user.home") + "/.earthlinguist/loaded_examples";

    /**
     * Stores examples that the user explicitly loads using the load examples button to be loaded by the Examples
     * class. This directory should be emptied when the app is opened and when the stored state is reverted.
     */
    public static final String SCRATCH_ZIPS = System.getProperty("user.home") + "/.earthlinguist/scratch_zips";
}
