package edu.kit.mima.gui.components.tabframe;

import edu.kit.mima.gui.components.alignment.Alignment;

import javax.swing.*;
import java.awt.*;

/**
 * Popup Component for {@link TabFrame}.
 *
 * @author Jannis Weis
 * @since 2019
 */
public abstract class PopupComponent extends JPanel {

    /**
     * Create new PopupComponent. PopupComponents are disabled by default as enabling should be
     * handled by the LayoutManager.
     */
    public PopupComponent() {
        setEnabled(false);
    }

    @Override
    public final Dimension getMinimumSize() {
        if (!isEnabled()) {
            return new Dimension(0, 0);
        } else {
            return getMinimumSizeImpl();
        }
    }

    protected abstract void setAlignment(final Alignment a, final boolean[] info);

    protected Dimension getMinimumSizeImpl() {
        return super.getMinimumSize();
    }

    @Override
    public final Dimension getPreferredSize() {
        if (!isEnabled()) {
            return new Dimension(0, 0);
        } else {
            return getPreferredSizeImpl();
        }
    }

    protected Dimension getPreferredSizeImpl() {
        return super.getPreferredSize();
    }

    public abstract void setCloseAction(Action action);

    public abstract void open();

    public abstract void close();
}
