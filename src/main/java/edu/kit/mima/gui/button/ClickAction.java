package edu.kit.mima.gui.button;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class ClickAction extends AbstractAction {

    private final JButton button;

    /**
     * Action wrapper for a button action
     *
     * @param button Button
     */
    public ClickAction(final JButton button) {
        this.button = button;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        button.doClick();
    }
}
