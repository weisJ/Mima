package edu.kit.mima.gui.components.tooltip;

import javax.swing.*;
import java.awt.*;

/**
 * DefaultTooltipWindow Window constrain class.
 *
 * @author Jannis Weis
 * @since 2019
 */
public abstract class TooltipWindow extends JWindow implements ITooltip {

    /**
     * TooltipWindow is always on top not focusable and by default has an invisible background.
     */
    public TooltipWindow() {
        setFocusable(false);
        setBackground(new Color(0, 0, 0, 0));
    }
}
