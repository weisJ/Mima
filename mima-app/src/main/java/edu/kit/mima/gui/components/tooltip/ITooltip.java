package edu.kit.mima.gui.components.tooltip;

import com.weis.darklaf.components.alignment.Alignment;

/**
 * DefaultTooltipWindow interface.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface ITooltip {

    /**
     * Get the alignment of the tooltip.
     *
     * @return {@link Alignment} of tooltip.
     */
    Alignment getAlignment();

    /**
     * Set the alignment for the tooltip.
     *
     * @param alignment new alignment.
     */
    void setAlignment(Alignment alignment);

    /**
     * Show the tooltip.
     */
    void showTooltip();

    /**
     * Hide the tooltip.
     */
    void hideTooltip();
}
