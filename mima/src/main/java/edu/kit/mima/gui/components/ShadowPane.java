package edu.kit.mima.gui.components;

import org.jdesktop.swingx.border.DropShadowBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JPanel;

/**
 * JPanel with shadow.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class ShadowPane extends JPanel {

    /**
     * Create new Shadow Pane with default shadow.
     */
    public ShadowPane() {
        this(10, 0.4f, 10);
    }

    /**
     * Create Shadow Pane with custom shadow.
     *
     * @param size       size of shadow.
     * @param opacity    opacity of shadow.
     * @param cornerSize corner radius of shadow.
     */
    public ShadowPane(final int size, final float opacity, final int cornerSize) {
        setLayout(new BorderLayout());
        setOpaque(false);
        final DropShadowBorder shadow = new DropShadowBorder(Color.BLACK,
                                                             size, opacity, cornerSize,
                                                             false,
                                                             true,
                                                             true,
                                                             true);
        setBorder(shadow);
    }
}
