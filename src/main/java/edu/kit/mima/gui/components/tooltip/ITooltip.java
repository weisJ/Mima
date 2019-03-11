package edu.kit.mima.gui.components.tooltip;

import edu.kit.mima.gui.components.Alignment;

/**
 * @author Jannis Weis
 * @since 2018
 */
public interface ITooltip {

    Alignment getAlignment();

    void setAlignment(Alignment alignment);

    void showTooltip();

}
