package edu.kit.mima.gui.components;

import org.jetbrains.annotations.NotNull;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.UIManager;

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
        if (value instanceof UIManager.LookAndFeelInfo) {
            value = ((UIManager.LookAndFeelInfo) value).getName();
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
}
