package io.github.cmuphil.earthlinguistfx.examples;

import io.github.cmuphil.earthlinguistfx.data.CheckboxRow;
import io.github.cmuphil.earthlinguistfx.data.TableData;
import io.github.cmuphil.earthlinguistfx.utils.Constants;
import io.github.cmuphil.earthlinguistfx.utils.ZipUtils;
import javafx.scene.control.Alert;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * A set of example tables for the EarthLinguist application. This class is a singleton.
 * <p>
 * The examples are loaded from the /examples directory in the classpath. Each example is a directory with the name
 * "example_N" where N is the example index. Each example directory contains a set of images and a checkmarks.txt file.
 * The checkmarks.txt file contains a matrix of 0s and 1s, where 0 indicates that the corresponding image is not
 * checked, and 1 indicates that the corresponding image is checked. The images are named 1.png, 2.png, 3.png, etc. The
 * images are displayed in the table view in the EarthLinguist application.
 * <p>
 * In the future, the examples will be loaded from a database.
 *
 * @author josephramsey
 */
public class Examples {

    /**
     * The singleton instance of this class.
     */
    private static Examples instance;

    /**
     * The example tables.
     */
    private Map<Integer, TableData> examplesMap;

    /**
     * Private constructor to prevent instantiation.
     */
    private Examples() {
        try {
            loadExamplesFromJar();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Error loading examples from jar: " + e.getMessage()).showAndWait();
        }
    }

    /**
     * Returns the singleton instance.
     *
     * @return the singleton instance.
     */
    public static Examples getInstance() {
        if (instance == null) {
            instance = new Examples();
        }
        return instance;
    }

    /**
     * Returns a list of example indices sorted in ascending order.
     *
     * @return a list of example indices.
     */
    public List<Integer> getExampleIndices() {
        List<Integer> exampleIndices = new ArrayList<>(examplesMap.keySet());
        Collections.sort(exampleIndices);
        return exampleIndices;
    }

    /**
     * Retrieves the example with the specified index from the "examples" map.
     *
     * @param index the index of the example to retrieve.
     * @return the example with the specified index.
     * @throws IllegalArgumentException if no example with the specified index exists.
     */
    public TableData getExample(int index) {
        if (!examplesMap.containsKey(index)) {
            throw new IllegalArgumentException("No example with index " + index + " exists.");
        }

        return examplesMap.get(index);
    }

    /**
     * Loads example data into the system from the "examples" directory in the resources.
     *
     * @throws IOException if there is an error in reading the files.
     */
    public void loadExamplesFromJar() throws IOException {
        URL examples = getClass().getClassLoader().getResource("examples");
        examplesMap = new HashMap<>();

        if (examples == null) {
            throw new RuntimeException("examples directory not found.");
        }

        for (int exampleIndex = 1; ; exampleIndex++) {
            URL example = getClass().getClassLoader().getResource("examples/example_" + exampleIndex);

            if (example == null) {
                break;
            }

            ClassLoader classLoader = getClass().getClassLoader();

            try (InputStream inputStream = classLoader.getResourceAsStream(
                    "examples/example_" + exampleIndex + "/checkmarks.txt")) {
                if (inputStream == null) {
                    throw new RuntimeException("checkmarks.txt not found in example directory.");
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                TableData tableData = null;

                String line;
                int numColumns = -1;
                int rowIndex = 0;

                while ((line = reader.readLine()) != null) {
                    ++rowIndex;

                    if (tableData == null) {
                        numColumns = line.length();
                        tableData = new TableData(numColumns);
                    }

                    if (line.length() != numColumns) {
                        throw new RuntimeException("Invalid checkmarks.txt dir: all lines must have the same number of columns.");
                    }

                    for (int j = 0; j < numColumns; j++) {
                        if (line.charAt(j) != '0' && line.charAt(j) != '1') {
                            throw new RuntimeException("Invalid checkmarks.txt dir: all characters must be 0 or 1.");
                        }
                    }

                    boolean[] checkmarks = new boolean[numColumns];

                    for (int j = 0; j < numColumns; j++) {
                        checkmarks[j] = line.charAt(j) == '1';
                    }

                    URL imageURL1 = getClass().getClassLoader().getResource("examples/example_" + exampleIndex + "/" + rowIndex + ".png");
                    URL imageURL2 = getClass().getClassLoader().getResource("examples/example_" + exampleIndex + "/" + rowIndex + ".jpg");

                    if (imageURL1 != null && imageURL2 != null) {
                        throw new RuntimeException("Multiple image files found in example directory for example " + exampleIndex +
                                                   " for row " + rowIndex + ".");
                    }

                    if (imageURL1 == null && imageURL2 == null) {
                        throw new RuntimeException("Image for row + " + rowIndex + " not found in example directory.");
                    }

                    String imagePath = "/examples/example_" + exampleIndex + "/" + rowIndex;

                    if (imageURL1 != null) {
                        imagePath += ".png";
                    } else {
                        imagePath += ".jpg";
                    }

                    tableData.addRow(new CheckboxRow(imagePath, numColumns, checkmarks));

                    examplesMap.put(exampleIndex, tableData);
                }
            }
        }
    }

    /**
     * Loads example data into the system from the specified zip file.
     *
     * @param zipFile the zip file.
     * @throws IOException              if there is an error in reading the files.
     * @throws IllegalArgumentException if the zip file does not contain properly formatted example data.
     */
    public void loadExamplesFromZip(File zipFile) throws IOException, IllegalArgumentException {
        File dir = new File(Constants.LOADED_EXAMPLES);

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Could not create the examples directory: " + Constants.LOADED_EXAMPLES);
            }
        }

        ZipUtils.deleteDirectoryContents(dir);
        ZipUtils.unzip(zipFile, dir);

        File[] loadedDirs = dir.listFiles();

        if (loadedDirs == null) {
            throw new IllegalArgumentException("Error reading examples directory.");
        }

        File examplesDir = null;

        for (File _dir : loadedDirs) {
            if (_dir.isDirectory()) {
                examplesDir = _dir;
                break;
            }
        }

        if (examplesDir == null) {
            throw new IllegalArgumentException("The unzip was unsuccessful; make sure you zipped up a directory.");
        }

        examplesMap = new HashMap<>();

        File[] files = examplesDir.listFiles();

        if (files == null) {
            throw new IllegalArgumentException("Error reading examples directory.");
        }

        for (File exampleDir : files) {
            if (exampleDir.getName().startsWith(".")) {
                continue;
            }

            String[] tokens = exampleDir.getName().split("_");

            if (tokens.length != 2) {
                throw new IllegalArgumentException("Invalid example directory: " + exampleDir.getName());
            }

            if (!tokens[0].equals("example")) {
                throw new IllegalArgumentException("Invalid example directory: " + exampleDir.getName());
            }

            if (!tokens[1].matches("\\d+")) {
                throw new IllegalArgumentException("Invalid example directory: " + exampleDir.getName());
            }

            int exampleIndex = Integer.parseInt(tokens[1]);

            File checkmarksFile = new File(exampleDir, "checkmarks.txt");

            if (!checkmarksFile.exists()) {
                throw new IllegalArgumentException("checkmarks.txt not found in example directory.");
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(checkmarksFile))) {
                TableData tableData = null;

                String line;
                int numColumns = -1;
                int rowIndex = 0;

                while ((line = reader.readLine()) != null) {
                    if (line.trim().isBlank()) {
                        continue;
                    }

                    ++rowIndex;

                    if (tableData == null) {
                        numColumns = line.length();
                        tableData = new TableData(numColumns);
                    }

                    if (line.length() != numColumns) {
                        throw new IllegalArgumentException("Invalid checkmarks.txt dir: all lines must have the same number of columns.");
                    }

                    for (int j = 0; j < numColumns; j++) {
                        if (line.charAt(j) != '0' && line.charAt(j) != '1') {
                            throw new IllegalArgumentException("Invalid checkmarks.txt dir: all characters must be 0 or 1.");
                        }
                    }

                    boolean[] checkmarks = new boolean[numColumns];

                    for (int j = 0; j < numColumns; j++) {
                        checkmarks[j] = line.charAt(j) == '1';
                    }

                    File imageFile = new File(exampleDir, rowIndex + ".png");

                    if (!imageFile.exists()) {
                        imageFile = new File(exampleDir, rowIndex + ".jpg");

                        if (!imageFile.exists()) {
                            throw new IllegalArgumentException("Image for example " + exampleIndex + ", row " + rowIndex
                                                               + " not found in example directory.");
                        }
                    }

                    tableData.addRow(new CheckboxRow(imageFile.getAbsolutePath(), numColumns, checkmarks));
                }

                examplesMap.put(exampleIndex, tableData);
            }
        }
    }
}
