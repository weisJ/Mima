package edu.kit.mima.gui.components.tooltip;

import edu.kit.mima.gui.components.ShadowPane;
import com.weis.darklaf.components.alignment.Alignment;
import com.weis.darklaf.components.border.TextBubbleBorder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/**
 * DefaultTooltipWindow Component with Shadow.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class DefaultTooltipWindow extends TooltipWindow {
    private final TextBubbleBorder bubbleBorder;
    private final JLabel textLabel;
    private final JPanel labelPanel;
    private final TooltipPanel tooltipPanel;
    private Alignment alignment;
    private float alpha = 0;
    private boolean backgroundSet = false;
    private boolean borderSet = false;


    /**
     * Create new DefaultTooltipWindow.
     *
     * @param text text of tooltip.
     */
    public DefaultTooltipWindow(@NotNull final String text) {
        setLayout(null);
        setAlwaysOnTop(true);

        alignment = Alignment.NORTH;
        tooltipPanel = new TooltipPanel();

        textLabel = new JLabel();
        textLabel.setOpaque(false);
        textLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        setText(text);

        bubbleBorder = new TextBubbleBorder(UIManager.getColor("Tooltip.borderColor"));
        bubbleBorder.setThickness(1).setPointerSize(10).setPointerWidth(8);

        labelPanel = new JPanel(new BorderLayout());
        labelPanel.setOpaque(false);
        labelPanel.add(textLabel, BorderLayout.CENTER);
        labelPanel.setBorder(bubbleBorder);
        labelPanel.setBackground(UIManager.getColor("Tooltip.background"));

        tooltipPanel.add(labelPanel);
        add(tooltipPanel);
    }

    public TooltipPanel getTooltipPanel() {
        return tooltipPanel;
    }

    public void setTooltipBackground(final Color c) {
        if (labelPanel == null) {
            return;
        }
        if (c != null) {
            backgroundSet = true;
            labelPanel.setBackground(c);
        } else {
            backgroundSet = false;
            labelPanel.setBackground(UIManager.getColor("Tooltip.background"));
        }
    }

    public void setTooltipBorderColor(final Color c) {
        if (bubbleBorder == null) {
            return;
        }
        if (c != null) {
            borderSet = true;
            bubbleBorder.setColor(c);
        } else {
            borderSet = false;
            bubbleBorder.setColor(UIManager.getColor("Tooltip.borderColor"));
        }
    }

    public void setTooltipForeground(final Color c) {
        if (textLabel == null) {
            return;
        }
        textLabel.setForeground(c);
    }

    public Font getTooltipFont() {
        return textLabel.getFont();
    }

    public void setTooltipFont(final Font font) {
        textLabel.setFont(font);
    }

    /**
     * Set the display text for the DefaultTooltipWindow.
     *
     * @param text tooltip text
     */
    public void setText(@NotNull final String text) {
        if (text.contains("\n")) {
            textLabel.setText("<html>" + text.replaceAll("\n", "<\\br>") + "</html>");
        } else {
            textLabel.setText("<html><body><nobr>" + text + "</nobr></body></html>");
        }
    }

    @Override
    public Alignment getAlignment() {
        return alignment;
    }

    @Override
    public void setAlignment(@NotNull final Alignment alignment) {
        this.alignment = alignment;
        bubbleBorder.setPointerSide(alignment.opposite());
    }

    /*
     * Apply insets to adjust position
     */
    @NotNull
    @Contract("_ -> param1")
    private Rectangle adjustAlignment(@NotNull final Rectangle bounds) {
        final int pointerSize = bubbleBorder.getPointerSize();
        final int pointerWidth = bubbleBorder.getPointerWidth();
        final int thickness = bubbleBorder.getThickness();
        switch (alignment) {
            case NORTH -> {
                bounds.y += pointerSize + 2 * thickness;
                bounds.x -= thickness;
                bounds.height -= thickness;
            }
            case NORTH_EAST -> {
                bounds.y += pointerSize;
                bounds.x -= 5 * pointerWidth - thickness;
                bubbleBorder.setPointerPadPercent(0);
            }
            case NORTH_WEST -> {
                bounds.y += pointerSize;
                bounds.x += 5 * pointerWidth - thickness;
                bubbleBorder.setPointerPadPercent(1);
            }
            case EAST -> {
                bounds.x -= pointerSize + thickness;
                bounds.y += pointerWidth / 2;
            }
            case WEST -> {
                bounds.y += pointerWidth / 2;
                bounds.x += pointerSize + thickness;
            }
            case SOUTH -> {
                bounds.x -= thickness;
                bounds.height -= thickness;
            }
            case SOUTH_EAST -> {
                bounds.x -= 5 * pointerWidth - thickness;
                bounds.height -= thickness;
                bubbleBorder.setPointerPadPercent(0);
            }
            case SOUTH_WEST -> {
                bounds.x -= thickness;
                bounds.height -= thickness;
                bounds.x += 5 * pointerWidth - thickness;
                bubbleBorder.setPointerPadPercent(1);
            }
            case CENTER -> {
                bounds.width -= thickness;
                bounds.height -= thickness;
            }
        }
        return bounds;
    }

    @Override
    public void setBounds(final int x, final int y, final int width, final int height) {
        if (tooltipPanel == null) {
            super.setBounds(x, y, width, height);
            return;
        }
        var r = adjustAlignment(new Rectangle(x, y, width, height));
        tooltipPanel.setBounds(0, 0, r.width, r.height);
        super.setBounds(r.x, r.y, r.width, r.height);
    }

    @Override
    public Dimension getPreferredSize() {
        return tooltipPanel.getPreferredSize();
    }

    @Override
    public void showTooltip() {
        setVisible(true);
        toFront();
        tooltipPanel.showTooltip(this);
    }

    @Override
    public void hideTooltip() {
        tooltipPanel.hideTooltip(this);
    }

    public void setRoundCorners(final boolean roundCorners) {
        if (roundCorners) {
            bubbleBorder.setRadius(4);
        } else {
            bubbleBorder.setRadius(0);
        }
    }

    public class TooltipPanel extends ShadowPane {


        private final ScheduledExecutorService scheduler = newSingleThreadScheduledExecutor();
        private ScheduledFuture<?> future;

        public void showTooltip(final Component parent) {
            setVisible(true);
            startFadeTimer(0, 1, 0.05f, parent);
        }

        public void hideTooltip(final Component parent) {
            startFadeTimer(1, 0, -0.05f, parent);
        }

        public void hideAndShow(final long millis, final Component parent) {
            if (future != null && !future.isDone()) {
                future.cancel(true);
            } else {
                hideTooltip(parent);
            }
            future = scheduler.schedule(() -> showTooltip(parent), millis, TimeUnit.MILLISECONDS);
        }

        private void startFadeTimer(final float start, final float end, final float increment,
                                    final Component parent) {
            alpha = start;
            final var timer = new Timer();
            final var task = new TimerTask() {
                @Override
                public void run() {
                    if (alpha == end) {
                        timer.cancel();
                    }
                    alpha = start < end
                            ? Math.min(alpha + increment, end)
                            : Math.max(alpha + increment, end);
                    if (alpha == 0) {
                        setVisible(false);
                        DefaultTooltipWindow.this.setVisible(false);
                    }
                    parent.repaint();
                }
            };
            timer.schedule(task, 0, 10);
        }


        @Override
        public void updateUI() {
            super.updateUI();
            if (labelPanel != null && !backgroundSet) {
                labelPanel.setBackground(UIManager.getColor("Tooltip.background"));
            }
            if (bubbleBorder != null && !borderSet) {
                bubbleBorder.setColor(UIManager.getColor("Tooltip.borderColor"));
            }
        }

        @Override
        protected void paintBorder(final Graphics g) {
            final int pointerSize = bubbleBorder.getPointerSize();
            final int thickness = bubbleBorder.getThickness();
            final Border border = getBorder();
            //Move shadow according to alignment.
            Rectangle rect = switch (alignment) {
                case NORTH, NORTH_EAST, NORTH_WEST -> new Rectangle(0, 0, getWidth(),
                                                                    getHeight() - pointerSize);
                case EAST -> new Rectangle(pointerSize, 0, getWidth() - pointerSize, getHeight());
                case SOUTH, SOUTH_EAST, SOUTH_WEST -> new Rectangle(0, pointerSize, getWidth(),
                                                                    getHeight() - pointerSize);
                case WEST -> new Rectangle(0, 0, getWidth() - pointerSize, getHeight());
                default -> new Rectangle(0, 0, getWidth(), getHeight());
            };
            rect.x += thickness;
            rect.y -= thickness;
            rect.width -= 2 * thickness;
            border.paintBorder(this, g, rect.x, rect.y, rect.width, rect.height);
        }

        @Override
        public void paint(@NotNull final Graphics g) {
            final Graphics2D g2d = (Graphics2D) g.create();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            super.paint(g2d);
            g2d.dispose();
        }
    }
}
