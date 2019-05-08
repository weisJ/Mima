package edu.kit.mima.gui.components;

import edu.kit.mima.gui.laf.components.MimaTableCellBorder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.function.BiConsumer;

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
    private final boolean editable = false;

    /**
     * Table that can be scrolled down. The fields of this table can not be edited by the user.
     *
     * @param tableHeader    header of table
     * @param initialEntries number of initial entries
     * @param cellInsets     the insets for cell display.
     */
    public FixedScrollTable(
            @NotNull final String[] tableHeader,
            final int initialEntries,
            @NotNull final Insets cellInsets) {
        this.tableHeader = tableHeader;
        table =
                new JTable(new DefaultTableModel(tableHeader, 0)) {
                    private final BiConsumer<JComponent, Integer> borderSetup =
                            (c, row) -> {
                                final var spacing =
                                        BorderFactory.createEmptyBorder(
                                                cellInsets.top, cellInsets.left, cellInsets.bottom, cellInsets.right);
                                if (isRowSelected(row)) {
                                    c.setBorder(new CompoundBorder(new SelectedBorder(), spacing));
                                } else {
                                    c.setBorder(spacing);
                                }
                            };

                    @Override
                    public boolean isCellEditable(final int row, final int column) {
                        return editable;
                    }

                    @NotNull
                    @Override
                    public Component prepareRenderer(
                            @NotNull final TableCellRenderer renderer, final int row, final int column) {
                        final var c = (JComponent) super.prepareRenderer(renderer, row, column);
                        borderSetup.accept(c, row);
                        return c;
                    }

                    @NotNull
                    @Override
                    public Component prepareEditor(@NotNull final TableCellEditor editor, final int row, final int column) {
                        var c = (JComponent) super.prepareEditor(editor, row, column);
                        borderSetup.accept(c, row);
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
        scrollPane.getVerticalScrollBar().setUnitIncrement(table.getRowHeight());

        final Object[][] data = new Object[initialEntries][tableHeader.length - 1];
        setContent(data);

        addComponentListener(
                new ComponentAdapter() {
                    @Override
                    public void componentResized(final ComponentEvent e) {
                        setBarInsets(new Insets(table.getTableHeader().getHeight(), 0, 0, 0));
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
        public void paintBorder(
                @NotNull final Component c, @NotNull final Graphics g, final int x, final int y, final int width, final int height) {
            g.setColor(table.getSelectionBackground());
            g.setClip(-2, 0, 2, c.getHeight());
            g.fillRect(-2, 0, 2, c.getHeight());
        }
    }
}
