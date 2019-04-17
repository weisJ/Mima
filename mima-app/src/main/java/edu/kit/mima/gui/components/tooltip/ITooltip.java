package edu.kit.mima.gui.components.tooltip;

import edu.kit.mima.gui.components.alignment.Alignment;

/**
 * Tooltip interface.
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

    /**
     * Returns whether the tooltip has been optimized.
     *
     * @return true if it has been optimized.
     */
    boolean isOptimized();

    /**
     * Set the optimized status of the tooltip.
     *
     * @param optimized the optimized status.
     */
    void setOptimized(boolean optimized);
}
