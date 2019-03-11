package edu.kit.mima.gui.components.fontchooser.panes;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import java.awt.Component;

public class ToolTipCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JComponent listCellRendererComponent = (JComponent) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        listCellRendererComponent.setToolTipText(value.toString());
        return listCellRendererComponent;
    }
}
