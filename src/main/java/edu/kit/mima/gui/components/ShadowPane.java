package edu.kit.mima.gui.components;

import org.jdesktop.swingx.border.DropShadowBorder;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class ShadowPane extends JPanel {

    public ShadowPane() {
        this(10, 0.4f, 10);
    }

    public ShadowPane(int size, float opacity, int cornerSize) {
        setLayout(new BorderLayout());
        setOpaque(false);
        DropShadowBorder shadow = new DropShadowBorder(Color.BLACK,
                size, opacity, cornerSize,
                false, true, true, true);
        setBorder(shadow);
    }
}
