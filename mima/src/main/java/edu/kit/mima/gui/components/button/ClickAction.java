package edu.kit.mima.gui.components.button;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;

/**
 * Wrapper for a button click action.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class ClickAction extends AbstractAction {

    private final JButton button;

    /**
     * Action wrapper for a button action.
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
