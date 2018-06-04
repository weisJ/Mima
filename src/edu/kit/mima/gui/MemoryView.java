package edu.kit.mima.gui;

import javax.swing.*;
import javax.swing.table.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class MemoryView extends JScrollPane {

    private String[] tableHeader;
    private JTable table;

    public MemoryView(String[] tableHeader) {
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

    public void setContent(Object[][] data) {
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        tableModel.setDataVector(data, tableHeader);
    }
}
