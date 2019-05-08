package edu.kit.mima.gui.components;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Component to draw at line index.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class IndexComponent extends JComponent {

    /**
     * Create new IndexComponent.
     */
    public IndexComponent() {
        setOpaque(false);
    }

    @Override
    public boolean isShowing() {
        return true;
    }

    @Nullable
    public Color getLineColor() {
        return null;
    }
}
