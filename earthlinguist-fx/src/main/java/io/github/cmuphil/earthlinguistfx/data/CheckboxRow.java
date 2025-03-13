package io.github.cmuphil.earthlinguistfx.data;

/**
 * Rrepresents a row in a TableData object containing a picture and a number of checkmarks.
 *
 * @author josephramsey
 */
public class CheckboxRow implements TableRow {

    /**
     * The path to the image file
     */
    private final String imagePath;

    /**
     * The number of checkmark columns in the row
     */
    private final int numCheckmarkColumns;

    /**
     * The checkmarks in the row, where checkmerks[i] == true iff the ith checkmark is checked.
     * The length of the array is numCheckmarkColumns.
     */
    private final boolean[] checkmarks;

    /**
     * Constructs a TableRow object.
     *
     * @param imagePath     the path to the image file.
     * @param numCheckmarks the number of checkmarks in the row.
     * @param checkmarks    the checkmarks in the row, a boolean array, where checkmerks[i] == true iff the ith
     *                      checkmark is checked.
     */
    public CheckboxRow(String imagePath, int numCheckmarks, boolean... checkmarks) {
        if (imagePath == null) {
            throw new IllegalArgumentException("Image path cannot be null.");
        }

        if (numCheckmarks < 1) {
            throw new IllegalArgumentException("Number of checkmarks must be at least 1.");
        }

        if (checkmarks.length != numCheckmarks) {
            throw new IllegalArgumentException("Number of checkmarks must match the number of checkmarks specified in the constructor.");
        }

        this.imagePath = imagePath;
        this.numCheckmarkColumns = numCheckmarks;
        this.checkmarks = checkmarks;
    }

    /**
     * Returns the path to the image file.
     *
     * @return the path to the image file.
     */
    public String getExampleImage() {
        return imagePath;
    }

    /**
     * Returns the number of checkmarks in the row.
     *
     * @return the number of checkmarks in the row.
     */
    public int getNumCheckmarkColumns() {
        return numCheckmarkColumns;
    }

    /**
     * Returns the image for the checkmark at the specified index, either a checkmark image or a blank image.
     *
     * @param colummIndex The column index.
     * @return the image for the checkmark at the specified index, either a checkmark image or a blank image.
     */
    public String getImage(int colummIndex) {
        return checkmarks[colummIndex]
                ? "/images/ui/checkmark.png"
                : "/images/ui/blank.png";
    }
}
