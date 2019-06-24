package edu.kit.mima.gui.components.tooltip;

import edu.kit.mima.gui.components.AlignPolicy;
import edu.kit.mima.gui.components.alignment.Alignment;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * DefaultTooltipWindow wrapper for handling the display management.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class TooltipComponent<T extends TooltipWindow> implements TooltipConstants {

    /*default*/
    @NotNull
    final T tooltip;
    /*default*/
    @NotNull
    final JComponent container;
    private final AlignPolicy centerAt;

    @NotNull
    private final TooltipEventHandler eventHandler;
    private boolean showOnce;

    private Point mousePos = new Point(0, 0);

    private Alignment alignmentOverwrite;

    /**
     * Register a tooltip component.
     *
     * @param container      container to attach tooltip to.
     * @param tooltip        tooltip of tooltip. Must be of type {@link JComponent} and implement the {@link
     *                       ITooltip} interface.
     * @param delay          display delay
     * @param vanishingDelay vanishing delay or {@link TooltipConstants#PERSISTENT}.
     * @param centerAt       one of {@link AlignPolicy}.
     */
    public TooltipComponent(@NotNull final JComponent container,
                            @NotNull final T tooltip,
                            final int delay,
                            final int vanishingDelay,
                            final AlignPolicy centerAt) {
        this.centerAt = centerAt;
        this.container = container;
        this.tooltip = tooltip;
        eventHandler = new TooltipEventHandler(this, delay, vanishingDelay);
    }

    /**
     * Set activation status of tooltip.
     *
     * @param active true if active
     */
    public void setActive(final boolean active) {
        this.eventHandler.setActive(active);
    }

    /**
     * Manually set the alignment.
     *
     * @param alignment the alignment to use.
     */
    public void setAlignment(final Alignment alignment) {
        this.alignmentOverwrite = alignment;
    }

    /**
     * Uninstall the DefaultTooltipWindow.
     */
    public void uninstall() {
        eventHandler.setActive(false);
    }

    /**
     * Show tooltip once and then remove it. Note: the given position has to be relative to the whole
     * screen.
     *
     * @param p position to show at.
     */
    public void showOnce(final Point p) {
        showOnce = true;
        showAt(p);
    }

    /**
     * Show the tooltip at the given position.
     *
     * @param p the position to show the tooltip at.
     */
    public void showAt(final Point p) {
        mousePos = p;
        showTooltipInternal();
    }

    /**
     * Show DefaultTooltipWindow at mousePosition.
     */
    public boolean showTooltip() {
        if (!showOnce) {
            mousePos = MouseInfo.getPointerInfo().getLocation();
        }
        if (SwingUtilities.getWindowAncestor(container) == null) {
            //container isn't connected to the hierarchy anymore. Remove the tooltip.
            uninstall();
            return false;
        }
        return showTooltipInternal();
    }

    /**
     * Hide the tooltip.
     */
    public void hideTooltip() {
        if (showOnce) {
            uninstall();
            showOnce = false;
            eventHandler.setActive(false);
        }
        tooltip.hideTooltip();
        tooltip.setVisible(false); // Ensure it's hidden
        if (container instanceof TooltipAware) {
            ((TooltipAware) container).setTooltipVisible(false, eventHandler);
        }
    }

    public void updateLocation() {
        var size = tooltip.getPreferredSize();
        var p = calculatePositionIn(container.getRootPane(), size, mousePos);
        var pos = new Point(p.x, p.y);
        SwingUtilities.convertPointToScreen(pos, container.getRootPane());
        tooltip.setBounds(pos.x, pos.y, size.width, size.height);
    }

    /*
     * Make the tooltip visible.
     */
    private boolean showTooltipInternal() {
        if (container instanceof TooltipAware) {
            ((TooltipAware) container).setTooltipVisible(true, eventHandler);
        }
        tooltip.setVisible(true);
        eventHandler.setActive(true);
        updateLocation();
        tooltip.showTooltip();
        return true;
    }

    /*
     * Calculate the position inside the given layer
     */
    @NotNull
    @Contract("_, _, _ -> new")
    private Point calculatePositionIn(@NotNull final Component c,
                                      @NotNull final Dimension size,
                                      @NotNull final Point mousePos) {
        final var containerPos = SwingUtilities.convertPoint(
                container, new Point(container.getWidth() / 2, container.getHeight() / 2), c);
        SwingUtilities.convertPointFromScreen(mousePos, c);
        var pos = centerAt.calculatePosition(mousePos, containerPos);
        Alignment alignment;
        if (alignmentOverwrite == null) {
            alignment = Alignment.getAlignment(pos, size, c.getBounds(), Alignment.SOUTH);
            alignment = alignment == Alignment.CENTER ? Alignment.SOUTH : alignment;
        } else {
            alignment = alignmentOverwrite;
        }
        tooltip.setAlignment(alignment);
        return alignment.relativePos(size, pos);
    }
}
