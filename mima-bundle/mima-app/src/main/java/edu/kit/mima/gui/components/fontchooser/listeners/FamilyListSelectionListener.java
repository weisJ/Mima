package edu.kit.mima.gui.components.fontchooser.listeners;

import edu.kit.mima.gui.components.fontchooser.FontContainer;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

/**
 * Created by dheid on 4/1/17.
 */
public class FamilyListSelectionListener implements ListSelectionListener {

    private final FontContainer fontContainer;

    public FamilyListSelectionListener(final FontContainer fontContainer) {
        this.fontContainer = fontContainer;
    }

    @Override
    public void valueChanged(@NotNull final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            final Font oldFont = fontContainer.getSelectedFont();
            final Font newFont =
                    new Font(
                            fontContainer.getSelectedFamily(),
                            oldFont.getStyle(),
                            (int) fontContainer.getSelectedSize());

            fontContainer.setSelectedFont(newFont);
            fontContainer.setPreviewFont(newFont);
        }
    }
}
