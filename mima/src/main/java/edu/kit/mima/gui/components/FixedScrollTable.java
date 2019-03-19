package edu.kit.mima.gui.components;

import org.jetbrains.annotations.NotNull;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Table with scroll bar that can't be edited by the user.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class FixedScrollTable extends JScrollPane {

    @NotNull
    private final String[] tableHeader;
    @NotNull
    private final JTable table;

    /**
     * Table that can be scrolled down. The fields of this table can not be edited by the user.
     *
     * @param tableHeader    header of table
     * @param initialEntries number of initial entries
     */
    public FixedScrollTable(@NotNull final String[] tableHeader, final int initialEntries) {
        super();
        this.tableHeader = tableHeader;
        table = new JTable(new DefaultTableModel(tableHeader, 0)) {
            @Override
            public boolean isCellEditable(final int row, final int column) {
                return false;
            }
        };
        table.setDragEnabled(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFillsViewportHeight(true);
        setViewportView(table);
        final Object[][] data = new Object[initialEntries][tableHeader.length - 1];
        setContent(data);
    }

    /**
     * Set the content of the table.
     *
     * @param data Data Vector
     */
    public void setContent(final Object[][] data) {
        final DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        tableModel.setDataVector(data, tableHeader);
    }
}
