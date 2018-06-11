package edu.kit.mima.gui;

import javax.swing.*;
import javax.swing.table.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class FixedScrollTable extends JScrollPane {

    private final String[] tableHeader;
    private final JTable table;

    /**
     * Table that can be scrolled down.
     * The fields of this table can not be edited by the user
     *
     * @param tableHeader header of table
     */
    public FixedScrollTable(final String[] tableHeader) {
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
    }

    /**
     * Set the content of the table
     *
     * @param data Data Vector
     */
    public void setContent(final Object[][] data) {
        final DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        tableModel.setDataVector(data, tableHeader);
    }
}
