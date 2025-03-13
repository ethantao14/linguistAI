package io.github.cmuphil.earthlinguistfx.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a table of images and checkmarks.
 *
 * @author josephramsey
 */
public class TableData {

    /**
     * The rows in the table.
     */
    private final List<CheckboxRow> rows = new ArrayList<>();

    /**
     * The number of checkmark columns in each row.
     */
    private final int numColumns;

    /**
     * Constructs a new TableData object.
     *
     * @param numColumns the number of checkmark columns in each row.
     */
    public TableData(int numColumns) {
        this.numColumns = numColumns;
    }

    /**
     * Adds a row to the data table.
     *
     * @param row the row to add.
     */
    public void addRow(CheckboxRow row) {
        if (row.getNumCheckmarkColumns() != numColumns) {
            throw new IllegalArgumentException("Number of checkmarks must match the number of checkmarks specified in the constructor.");
        }

        rows.add(row);
    }

    /**
     * Returns the number of checkmark columns in each row.
     *
     * @return the number of checkmark columns in each row.
     */
    public int getNumColumns() {
        return numColumns;
    }

    /**
     * Returns the row at the specified index.
     *
     * @param columnIndex the index of the row.
     * @return the row at the specified index.
     */
    public CheckboxRow getRow(int columnIndex) {
        return rows.get(columnIndex);
    }

    /**
     * Returns the rows of the tqble (excluding the header).
     *
     * @return the rows of the tqble (excluding the header).
     */
    public List<CheckboxRow> getRows() {
        return new ArrayList<>(rows);
    }
}
