package edu.kit.mima.gui.components;

import com.bulenkov.iconloader.util.EmptyIcon;
import com.bulenkov.iconloader.util.Pair;
import edu.kit.mima.gui.laf.components.MimaTableCellBorder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Table with scroll bar that can't be edited by the user.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class ProtectedScrollTable extends BorderlessScrollPane {

    @NotNull
    private final String[] tableHeader;
    private final Map<Pair<Integer, Integer>, Icon> iconMap;
    private final Map<Integer, Integer> maxIconSize;
    @NotNull
    private final Insets cellInsets;
    @NotNull
    private final JTable table;

    /**
     * Table that can be scrolled down. The fields of this table can not be edited by the user.
     *
     * @param tableHeader    header of table
     * @param initialEntries number of initial entries
     * @param cellInsets     the insets for cell display.
     */
    public ProtectedScrollTable(
            @NotNull final String[] tableHeader,
            final int initialEntries,
            @NotNull final Insets cellInsets) {
        this.tableHeader = tableHeader;
        this.cellInsets = cellInsets;

        maxIconSize = new HashMap<>();
        table = new StyledTable();
        iconMap = new HashMap<>();

        JTextField textField = new JTextField();
        textField.setBorder(new MimaTableCellBorder());
        DefaultCellEditor editor = new DefaultCellEditor(textField);
        table.setDefaultEditor(Object.class, editor);
        table.setShowGrid(false);
        table.setDragEnabled(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFillsViewportHeight(true);
        table.setRowHeight(20);

        scrollPane.setViewportView(table);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().putClientProperty("ScrollBar.thin", Boolean.TRUE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(table.getRowHeight());

        final Object[][] data = new Object[initialEntries][tableHeader.length - 1];
        setContent(data);

        addComponentListener(new ComponentAdapter() {
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

    public void setIcon(final Icon icon, final int row, final int column) {
        iconMap.put(new Pair<>(row, column), icon);
        maxIconSize.put(column, Math.max(icon.getIconWidth(), maxIconSize.getOrDefault(column, 0)));
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
     * Clear all displayed icons.
     */
    public void clearIcons() {
        iconMap.clear();
        maxIconSize.clear();
    }

    /**
     * Border that fixes the missing vertical selection color.
     */
    private final class SelectedBorder extends AbstractBorder {

        @Override
        public void paintBorder(@NotNull final Component c, @NotNull final Graphics g,
                                final int x, final int y,
                                final int width, final int height) {
            var g2 = g.create();
            g2.setColor(table.getSelectionBackground());
            g2.setClip(-2, 0, 2, c.getHeight());
            g2.fillRect(-2, 0, 2, c.getHeight());
            g2.dispose();
        }
    }

    private final class StyledTable extends JTable {

        private final Border spacing = BorderFactory.createEmptyBorder(cellInsets.top, cellInsets.left,
                                                                       cellInsets.bottom, cellInsets.right);

        private void borderSetup(@NotNull final JComponent c, final int row) {
            if (isRowSelected(row)) {
                c.setBorder(new CompoundBorder(new SelectedBorder(), spacing));
            } else {
                c.setBorder(spacing);
            }
        }

        @Nullable
        @Override
        public Color getSelectionBackground() {
            if (isFocusOwner()) {
                return super.getSelectionBackground();
            } else {
                return UIManager.getColor("Table.selectedBackground.noFocus");
            }
        }

        private Icon getIcon(final int r, final int c) {
            var icon = iconMap.get(new Pair<>(r, c));
            var emptySize = maxIconSize.getOrDefault(c, 0);
            return icon != null ? icon : new EmptyIcon(emptySize, emptySize);
        }

        private boolean isCellFocused(final int row, final int column) {
            boolean rowIsLead =
                    (selectionModel.getLeadSelectionIndex() == row);
            boolean colIsLead =
                    (columnModel.getSelectionModel().getLeadSelectionIndex() == column);
            return rowIsLead && colIsLead && isFocusOwner();
        }

        @Contract(pure = true)
        @Override
        public boolean isCellEditable(final int row, final int column) {
            return false;
        }

        private Component prepareView(final JComponent c, final int row, final int column) {
            var icon = getIcon(row, column);
            int iconGap = isCellSelected(row, column) && isCellFocused(row, column) ? 2 : 1;
            if (maxIconSize.containsKey(column)) {
                iconGap += icon.getIconWidth() - maxIconSize.get(column);
            }
            final var iconComp = new IconComponent<>(c, icon, IconComponent.LEFT, 3, iconGap);
            borderSetup(iconComp, row);
            iconComp.setBackground(c.getBackground());
            iconComp.setOpaque(true);
            return iconComp;
        }


        @NotNull
        @Override
        public Component prepareRenderer(@NotNull final TableCellRenderer renderer,
                                         final int row, final int column) {
            final var c = (JComponent) super.prepareRenderer(renderer, row, column);
            return prepareView(c, row, column);
        }

        @NotNull
        @Override
        public Component prepareEditor(@NotNull final TableCellEditor editor,
                                       final int row, final int column) {
            var c = (JComponent) super.prepareEditor(editor, row, column);
            return prepareView(c, row, column);
        }
    }
}
