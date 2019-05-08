package edu.kit.mima.gui.components.fontchooser.panes;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class ToolTipCellRenderer extends DefaultListCellRenderer {

    @NotNull
    @Override
    public Component getListCellRendererComponent(
            @NotNull final JList<?> list,
            @NotNull final Object value,
            final int index,
            final boolean isSelected,
            final boolean cellHasFocus) {
        final JComponent listCellRendererComponent =
                (JComponent)
                        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        listCellRendererComponent.setToolTipText(value.toString());
        return listCellRendererComponent;
    }
}
