package io.github.cmuphil.earthlinguistfx.utils;

import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static io.github.cmuphil.earthlinguistfx.utils.UiUtils.alert;

/**
 * Some utilities for zipping.
 *
 * @author josephramsey
 */
public class ZipUtils {

    /**
     * Zips the given directory.
     *
     * @param dir     the directory to zip.
     * @param zipFile the zip file to create.
     */
    public static void zip(File dir, File zipFile) {
        Path sourceDir = dir.toPath();

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                alert(Alert.AlertType.ERROR, "Error creating directory: " + dir);
                throw new RuntimeException("Error creating directory: " + dir);
            }
        } else if (!dir.isDirectory()) {
            alert(Alert.AlertType.ERROR, "The source directory is not a directory: " + dir);
            throw new RuntimeException("The source directory is not a directory: " + dir);
        }

        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            Files.walkFileTree(sourceDir, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE,
                    new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            // Only regular files should be added to the zip
                            if (attrs.isRegularFile()) {
                                // Get the relative path to the source directory
                                Path relativePath = sourceDir.relativize(file);
                                zos.putNextEntry(new ZipEntry(relativePath.toString()));
                                byte[] bytes = Files.readAllBytes(file);
                                zos.write(bytes, 0, bytes.length);
                                zos.closeEntry();
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Extracts the given file from the zip file.
     *
     * @param zipFile   the zip file.
     * @param targetDir the directory where the file will be extracted.
     * @throws IOException if there is an error extracting the file.
     */
    public static void unzip(File zipFile, File targetDir) throws IOException {

        if (!targetDir.exists()) {
            if (!targetDir.mkdirs()) {
                alert(Alert.AlertType.ERROR, "Error creating directory: " + targetDir);
                throw new RuntimeException("Error creating directory: " + targetDir);
            }
        }

        if (zipFile == null || zipFile.getName().isBlank()) {
            throw new NullPointerException("The zip file name is not available.");
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry = zis.getNextEntry();
            byte[] buffer = new byte[1024];

            while (zipEntry != null) {
                File newFile = newFile(targetDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {

                    // Create parent directories
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // Write file content
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }


    /**
     * Creates a new file in the given directory.
     *
     * @param destinationDir the directory where the file will be created.
     * @param zipEntry       the zip entry.
     * @return the new file.
     * @throws IOException if there is an error creating the file.
     */
    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    /**
     * Returns the next zip file name for the given directory and name.
     *
     * @param zipFilesDir the directory where the zip files are saved.
     * @param name        the name of the zip file.
     * @return the next zip file name for the given directory and name.
     */
    public static String nextZipName(String zipFilesDir, String name) {
        for (int i = 1; ; i++) {
            String _name = name + "." + i + ".zip";
            _name = _name.replace(" ", "_").toLowerCase();

            File file = new File(zipFilesDir, _name);

            if (!file.exists()) {
                return _name;
            }
        }
    }

    /**
     * Deletes the contents of the given directory.
     *
     * @param dir the directory.
     */
    public static void deleteDirectoryContents(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) { // some JVMs return null for empty directories
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectoryContents(file);
                    }
                    boolean delete = file.delete();

                    if (!delete) {
                        alert(Alert.AlertType.ERROR, "Error deleting file: " + file.getName());
                        throw new RuntimeException("Error deleting file: " + file.getName());
                    }
                }
            }
        }
    }

    /**
     * Zips the clips in the scratch directory and saves the zip file to the save directory.
     *
     * @param selectedExample  the name of the example.
     * @param selectedLanguage the name of the language.
     * @param primaryStage     the primary stage.
     */
    public static File save(Integer selectedExample, String selectedLanguage, String selectedCountry, Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        File userHome = new File(System.getProperty("user.home"));
        fileChooser.setInitialDirectory(userHome);

        String clipsFilename = ("example " + selectedExample + " " + selectedLanguage
                + " " + selectedCountry).replace(" ", "_").toLowerCase();

        String next = ZipUtils.nextZipName(userHome.getAbsolutePath(), clipsFilename);

        fileChooser.setInitialFileName(next);

        // Set extension filter, if you want to restrict the file type
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("ZIP files (*.zip)", "*.zip");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show save file dialog
        File fileToSave = fileChooser.showSaveDialog(primaryStage);

        if (fileToSave != null) {
            String clipsFilename2 = fileToSave.getName();
            File file = new File(fileToSave.getParent(), clipsFilename2);
            UiUtils.recreateFile(clipsFilename, file);
            zip(new File(Constants.SCRATCH_FILES_DIRECTORY_RECORD), file);

            return file;
        }

        return null;
    }
}
