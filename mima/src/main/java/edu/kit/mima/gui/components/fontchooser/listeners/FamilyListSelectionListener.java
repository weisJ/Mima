package edu.kit.mima.gui.components.fontchooser.listeners;

import edu.kit.mima.gui.components.fontchooser.FontContainer;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Font;


/**
 * Created by dheid on 4/1/17.
 */
public class FamilyListSelectionListener implements ListSelectionListener {

    private final FontContainer fontContainer;

    public FamilyListSelectionListener(FontContainer fontContainer) {
        this.fontContainer = fontContainer;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            Font oldFont = fontContainer.getSelectedFont();
            Font newFont = new Font(
                    fontContainer.getSelectedFamily(),
                    oldFont.getStyle(),
                    (int) fontContainer.getSelectedSize());

            fontContainer.setSelectedFont(newFont);
            fontContainer.setPreviewFont(newFont);
        }
    }
}
