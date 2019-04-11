package edu.kit.mima.gui.components;

import edu.kit.mima.gui.laf.components.MimaTableCellBorder;
import org.jetbrains.annotations.NotNull;

import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.AbstractBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Table with scroll bar that can't be edited by the user.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class FixedScrollTable extends BorderlessScrollPane {

    @NotNull
    private final String[] tableHeader;
    @NotNull
    private final JTable table;
    private boolean editable = false;

    /**
     * Table that can be scrolled down. The fields of this table can not be edited by the user.
     *
     * @param tableHeader    header of table
     * @param initialEntries number of initial entries
     */
    public FixedScrollTable(@NotNull final String[] tableHeader, final int initialEntries) {
        this.tableHeader = tableHeader;
        table = new JTable(new DefaultTableModel(tableHeader, 0)) {
            @Override
            public boolean isCellEditable(final int row, final int column) {
                return editable;
            }

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                var c = (JComponent) super.prepareRenderer(renderer, row, column);
                if (isRowSelected(row)) {
                    c.setBorder(new SelectedBorder());
                } else {
                    c.setBorder(null);
                }
                return c;
            }

            @Override
            public Component prepareEditor(TableCellEditor editor, int row, int column) {
                var c = (JComponent) super.prepareEditor(editor, row, column);
                if (isRowSelected(row)) {
                    c.setBorder(new SelectedBorder());
                } else {
                    c.setBorder(null);
                }
                return c;
            }
        };
        JTextField textField = new JTextField();
        textField.setBorder(new MimaTableCellBorder());
        DefaultCellEditor editor = new DefaultCellEditor(textField);
        table.setDefaultEditor(Object.class, editor);
        table.setShowGrid(false);
        table.setDragEnabled(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFillsViewportHeight(true);

        scrollPane.setViewportView(table);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().putClientProperty("ScrollBar.thin", Boolean.TRUE);

        final Object[][] data = new Object[initialEntries][tableHeader.length - 1];
        setContent(data);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                setBarInsets(new Insets(table.getTableHeader().getHeight(), 0,
                                        0, 0));
            }
        });
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

    /**
     * Get the table.
     *
     * @return the table.
     */
    @NotNull
    public JTable getTable() {
        return table;
    }

    /**
     * Border that fixes the missing vertical selection color.
     */
    private class SelectedBorder extends AbstractBorder {

        @Override
        public void paintBorder(@NotNull Component c, @NotNull Graphics g,
                                int x, int y, int width, int height) {
            g.setColor(table.getSelectionBackground());
            g.setClip(-2, 0, 2, c.getHeight());
            g.fillRect(-2, 0, 2, c.getHeight());
        }
    }
}
