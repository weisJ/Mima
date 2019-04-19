package edu.kit.mima.gui.laf.components;

import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import java.awt.Component;
import java.awt.Insets;

/**
 * Table cell border.
 * Prevents content to be shifted when selected.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class MimaTableCellBorder extends LineBorder {


    /**
     * Create new Cell border.
     */
    public MimaTableCellBorder() {
        super(UIManager.getColor("Border.line1"));
    }

    @Override
    public Insets getBorderInsets(Component c) {
        var i = super.getBorderInsets(c);
        i.left += 1;
        return i;
    }
}