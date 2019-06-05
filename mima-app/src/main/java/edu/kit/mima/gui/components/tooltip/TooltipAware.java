package edu.kit.mima.gui.components.tooltip;

/**
 * Interface for tooltip aware components.
 *
 * @author Jannis Weis
 * @since 2019
 */
public interface TooltipAware {

    void setTooltipVisible(final boolean visible, final TooltipEventHandler eventHandler);
}
