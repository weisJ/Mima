package edu.kit.mima.gui.components;

import org.jetbrains.annotations.NotNull;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.UIManager;
import java.awt.Component;

/**
 * Cell renderer for {@link javax.swing.UIManager.LookAndFeelInfo}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class LookAndFeelInfoCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(@NotNull final JList<?> list,
                                                  Object value,
                                                  final int index,
                                                  final boolean isSelected,
                                                  final boolean cellHasFocus) {
        Object value1 = value;
        if (value1 instanceof UIManager.LookAndFeelInfo) {
            value1 = ((UIManager.LookAndFeelInfo) value1).getName();
        }
        return super.getListCellRendererComponent(list, value1, index, isSelected, cellHasFocus);
    }
}
