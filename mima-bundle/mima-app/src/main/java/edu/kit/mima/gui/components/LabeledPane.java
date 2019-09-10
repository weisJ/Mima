package edu.kit.mima.gui.components;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Pane for component with label.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class LabeledPane extends JPanel {

    /**
     * Create new Panel with component and given label.
     *
     * @param panel component
     * @param label label
     */
    public LabeledPane(@NotNull final JComponent panel, final String label) {
        var l = new JLabel(label);
        l.setLabelFor(panel);
        setLayout(new GridBagLayout());
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        gridBagConstraints.gridy = 0;
        add(l, gridBagConstraints);
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        add(panel, gridBagConstraints);
    }

    @Override
    public int getBaseline(final int width, final int height) {
        return 0;
    }
}
