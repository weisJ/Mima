package edu.kit.mima.gui.components.tooltip;

import edu.kit.mima.gui.components.AlignPolicy;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class for showing tooltips.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class TooltipUtil {

    private static final int DELAY = 1000;
    private static final int VANISH_DELAY = 2000;

    @Contract(" -> fail")
    private TooltipUtil() {
        assert false : "utility class constructor";
    }

    public static <T extends TooltipWindow> void createDefaultTooltip(final JComponent c, final T tooltip) {
        new TooltipComponent<>(c, tooltip, DELAY, VANISH_DELAY, AlignPolicy.COMPONENT_BOTH).setActive(true);
    }

    /**
     * Show tooltip at given current mouse position.
     *
     * @param tooltip DefaultTooltipWindow to show.
     * @param <T>     DefaultTooltipWindow type.
     * @return the TooltipComponent controlling the tooltip.
     */
    public static <T extends TooltipWindow> TooltipComponent<T> showTooltip(@NotNull final T tooltip) {
        final Window window = findWindow();
        if (window instanceof JFrame) {
            final Point p = MouseInfo.getPointerInfo().getLocation();
            return showTooltip(tooltip, ((JFrame) window).getRootPane(), p, false, AlignPolicy.MOUSE_BOTH);
        }
        return null;
    }

    /**
     * Show tooltip at given position.
     *
     * @param tooltip DefaultTooltipWindow to show.
     * @param p       position to show at.
     * @param permanent whether the tooltip should be permanent.
     * @param <T>     DefaultTooltipWindow type.
     * @return the TooltipComponent controlling the tooltip.
     */
    public static <T extends TooltipWindow> TooltipComponent<T> showTooltip(@NotNull final T tooltip, final Point p,
                                                                            final boolean permanent) {
        final Window window = findWindow();
        if (window instanceof JFrame) {
            return showTooltip(tooltip, ((JFrame) window).getRootPane(), p, permanent, AlignPolicy.MOUSE_BOTH);
        }
        return null;
    }

    /**
     * Show the given tooltip once.
     *
     * @param tooltip   DefaultTooltipWindow to show.
     * @param container container to tooltip should be hooked to.
     * @param p         position to show at. Choosing a alignment relative to the component may ignore the x
     *                  or y value of the point.
     * @param permanent whether the tooltip should be permanent.
     * @param alignAt   alignment of tooltip relative to mouse/component.
     * @param <T>       DefaultTooltipWindow type.
     * @return the TooltipComponent controlling the tooltip.
     */
    public static <T extends TooltipWindow> TooltipComponent<T> showTooltip(@NotNull final T tooltip,
                                                                            @NotNull final JComponent container,
                                                                            final Point p,
                                                                            final boolean permanent,
                                                                            final AlignPolicy alignAt) {
        final var tooltipComponent = new TooltipComponent<>(container, tooltip, 0,
                                                            permanent ? TooltipConstants.PERSISTENT : 2000, alignAt);
        tooltipComponent.showOnce(p);
        return tooltipComponent;
    }

    /*
     * Find current window of mouse.
     */
    @Nullable
    private static Window findWindow() {
        for (final Window window : Window.getWindows()) {
            if (window.getMousePosition(true) != null) {
                return window;
            }
        }
        return null;
    }
}
