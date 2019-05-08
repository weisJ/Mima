package edu.kit.mima.gui.components.fontchooser.listeners;

import edu.kit.mima.gui.components.fontchooser.FontContainer;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

/**
 * Created by dheid on 4/1/17.
 */
public class SizeListSelectionListener implements ListSelectionListener {

    private final FontContainer fontContainer;

    public SizeListSelectionListener(final FontContainer fontContainer) {
        this.fontContainer = fontContainer;
    }

    @Override
    public void valueChanged(@NotNull final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            final float newSize = fontContainer.getSelectedSize();
            final Font newFont = fontContainer.getSelectedFont().deriveFont(newSize);
            fontContainer.setSelectedFont(newFont);
            fontContainer.setPreviewFont(fontContainer.getSelectedFont());
        }
    }
}
