package edu.kit.mima.gui.components;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.UIManager;
import java.awt.Component;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class LookAndFeelInfoCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof UIManager.LookAndFeelInfo) {
            value = ((UIManager.LookAndFeelInfo) value).getName();
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
}
