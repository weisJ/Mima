package edu.kit.mima.gui.components.tooltip;

import edu.kit.mima.gui.components.AlignPolicy;
import edu.kit.mima.gui.components.Alignment;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Tooltip wrapper for handling the display management.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class TooltipComponent<T extends JComponent & ITooltip>
        extends MouseAdapter implements TooltipConstants {

    /*default*/ final T content;
    /*default*/ final JComponent container;
    private final AlignPolicy centerAt;
    private final TooltipEventHandler eventHandler;

    private boolean installed;
    private boolean showOnce;

    private Point mousePos = new Point(0, 0);

    /**
     * Register a tooltip component.
     *
     * @param container      container to attach tooltip to.
     * @param content        content of tooltip. Must be of type {@link JComponent} and implement
     *                       the {@link ITooltip} interface.
     * @param delay          display delay
     * @param vanishingDelay vanishing delay or {@link TooltipConstants#PERSISTENT}.
     * @param centerAt       one of {@link AlignPolicy}.
     */
    public TooltipComponent(
            @NotNull final JComponent container,
            @NotNull final T content,
            final int delay,
            final int vanishingDelay,
            final AlignPolicy centerAt) {
        this.centerAt = centerAt;
        this.container = container;
        this.content = content;
        eventHandler = new TooltipEventHandler(this, delay, vanishingDelay);
        content.setOpaque(false);
    }

    /**
     * Set activation status of tooltip.
     *
     * @param active true if active
     */
    public void setActive(final boolean active) {
        this.eventHandler.setActive(active);
    }

    /*
     * Install tooltip
     */
    private void install() {
        final JPanel layer = (JPanel) container.getRootPane().getGlassPane();
        final Dimension size = container.getRootPane().getSize();
        if (layer.getLayout() != null) {
            layer.setLayout(null);
            layer.setPreferredSize(size);
        }
        layer.add(content);
        installed = true;
    }

    /**
     * Uninstall the Tooltip.
     */
    public void uninstall() {
        final var root = container.getRootPane();
        final JPanel layer = (JPanel) root.getGlassPane();
        content.setVisible(false);
        container.removeMouseListener(this);
        layer.remove(content);
        layer.revalidate();
        layer.repaint();
        installed = false;
        eventHandler.setActive(false);
    }

    /**
     * Show tooltip once and then remove it. Note: the given position has to be relative to the
     * whole screen.
     *
     * @param p position to show at.
     */
    public void showOnce(final Point p) {
        showOnce = true;
        mousePos = p;
        eventHandler.setActive(true);
        mouseEntered(null);
    }

    /**
     * Show Tooltip at mousePosition.
     */
    public void showTooltip() {
        if (!showOnce) {
            mousePos = MouseInfo.getPointerInfo().getLocation();
        }
        showTooltipInternal();
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
        content.hideTooltip();
        container.repaint();
    }

    /*
     * Make the tooltip visible.
     */
    private void showTooltipInternal() {
        eventHandler.setActive(true);
        if (!installed) {
            install();
        }
        final var root = container.getRootPane();
        final JPanel layer = (JPanel) root.getGlassPane();
        final var size = content.getPreferredSize();
        final var p = calculatePositionIn(layer, size, mousePos);
        content.showTooltip();
        content.setBounds(p.x, p.y, size.width, size.height);
        content.revalidate();
        content.repaint();
        layer.setVisible(true);
        layer.repaint();
        container.repaint();
    }

    /*
     * Calculate the position inside the given layer
     */
    @NotNull
    @Contract("_, _, _ -> new")
    private Point calculatePositionIn(@NotNull final JComponent layer,
                                      @NotNull final Dimension size,
                                      @NotNull final Point mousePos) {
        final var containerPos = SwingUtilities
                .convertPoint(container,
                              new Point(container.getWidth() / 2, container.getHeight() / 2),
                              layer);
        SwingUtilities.convertPointFromScreen(mousePos, layer);
        var pos = centerAt.calculatePosition(mousePos, containerPos);
        Alignment alignment = Alignment.getAlignment(pos, size, layer.getBounds(), Alignment.SOUTH);
        content.setAlignment(alignment);
        return alignment.relativePos(size, pos);
    }
}
