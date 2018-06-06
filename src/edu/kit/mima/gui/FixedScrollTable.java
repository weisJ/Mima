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
     * The fieds of this table can not be edited by the user
     *
     * @param tableHeader header of table
     */
    public FixedScrollTable(final String[] tableHeader) {
        this.tableHeader = tableHeader;
        table = new JTable(new DefaultTableModel(tableHeader, 0)) {
            @Override
            public boolean isCellEditable(int row, int column) {
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
    public void setContent(Object[][] data) {
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        tableModel.setDataVector(data, tableHeader);
    }
}
