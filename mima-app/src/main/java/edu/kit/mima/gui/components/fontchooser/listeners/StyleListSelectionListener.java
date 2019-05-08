package edu.kit.mima.gui.components.fontchooser.listeners;

import edu.kit.mima.gui.components.fontchooser.FontContainer;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

/**
 * Created by dheid on 4/1/17.
 */
public class StyleListSelectionListener implements ListSelectionListener {

    private final FontContainer fontContainer;

    public StyleListSelectionListener(final FontContainer fontContainer) {
        this.fontContainer = fontContainer;
    }

    @Override
    public void valueChanged(@NotNull final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            final String selectedStyle = fontContainer.getSelectedStyle();
            final Font oldFont = fontContainer.getSelectedFont();
            final Font newFont = new Font(selectedStyle, oldFont.getStyle(), oldFont.getSize());
            fontContainer.setSelectedFont(newFont);
            fontContainer.setPreviewFont(newFont);
        }
    }
}
