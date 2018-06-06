package edu.kit.mima.gui.button;

import javax.swing.*;
import java.awt.event.*;

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
    public ClickAction(JButton button) {
        this.button = button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        button.doClick();
    }
}
