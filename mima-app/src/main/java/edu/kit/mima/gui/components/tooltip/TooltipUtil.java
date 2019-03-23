package edu.kit.mima.gui.components.tooltip;

import edu.kit.mima.gui.components.AlignPolicy;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Window;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * Utility class for showing tooltips.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class TooltipUtil {

    @Contract(" -> fail")
    private TooltipUtil() {
        assert false : "utility class constructor";
    }

    /**
     * Show tooltip at given current mouse position.
     *
     * @param tooltip Tooltip to show.
     * @param <T>     Tooltip must be of type {@link JComponent} and implement {@link ITooltip}.
     */
    public static <T extends JComponent & ITooltip> void showTooltip(@NotNull final T tooltip) {
        final Window window = findWindow();
        if (window instanceof JFrame) {
            final Point p = MouseInfo.getPointerInfo().getLocation();
            showTooltip(tooltip, ((JFrame) window).getRootPane(), p, AlignPolicy.MOUSE_BOTH);
        }
    }


    /**
     * Show tooltip at given position.
     *
     * @param tooltip Tooltip to show.
     * @param p       position to show at.
     * @param <T>     Tooltip must be of type {@link JComponent} and implement {@link ITooltip}.
     */
    public static <T extends JComponent & ITooltip> void showTooltip(@NotNull final T tooltip,
                                                                     final Point p) {
        final Window window = findWindow();
        if (window instanceof JFrame) {
            showTooltip(tooltip, ((JFrame) window).getRootPane(), p, AlignPolicy.MOUSE_BOTH);
        }
    }

    /**
     * Show the given tooltip once.
     *
     * @param tooltip   Tooltip to show.
     * @param container container to tooltip should be hooked to.
     * @param p         position to show at. Choosing a alignment relative to the component may
     *                  ignore the x or y value of the point.
     * @param alignAt   alignment of tooltip relative to mouse/component.
     * @param <T>       Tooltip must be of type {@link JComponent} and implement {@link ITooltip}.
     */
    public static <T extends JComponent & ITooltip> void showTooltip(
            @NotNull final T tooltip,
            @NotNull final JComponent container,
            final Point p,
            final AlignPolicy alignAt) {
        final var tooltipComponent = new TooltipComponent<>(container, tooltip,
                                                            0, 2000, alignAt);
        tooltipComponent.showOnce(p);
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
