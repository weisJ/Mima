package edu.kit.mima.gui.components.tooltip;

import edu.kit.mima.gui.components.ShadowPane;
import edu.kit.mima.gui.components.TextBubbleBorder;
import edu.kit.mima.gui.components.alignment.Alignment;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Tooltip Component with Shadow.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class Tooltip extends ShadowPane implements ITooltip {
    private final TextBubbleBorder bubbleBorder;
    private final JLabel textLabel;
    private final JPanel labelPanel;
    private Alignment alignment;
    private float alpha = 0;
    private boolean optimized = false;

    /**
     * Create new Tooltip.
     *
     * @param text text of tooltip.
     */
    public Tooltip(@NotNull final String text) {
        alignment = Alignment.NORTH;
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

        add(labelPanel);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (labelPanel != null) {
            labelPanel.setBackground(UIManager.getColor("Tooltip.background"));
        }
        if (bubbleBorder != null) {
            bubbleBorder.setColor(UIManager.getColor("Tooltip.borderColor"));
        }
    }

    /**
     * Set the display text for the Tooltip.
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
    private Rectangle adjustAlignment(@NotNull Rectangle bounds) {
        final int pointerSize = bubbleBorder.getPointerSize();
        switch (alignment) {
            case NORTH_WEST, SOUTH_WEST -> {
                bounds.x += 4 * pointerSize;
                bubbleBorder.setPointerPadPercent(1);
            }
            case NORTH_EAST, SOUTH_EAST -> {
                bounds.x -= 4 * pointerSize;
                bubbleBorder.setPointerPadPercent(0);
            }
            default -> {
            }
        }
        switch (alignment) {
            case NORTH, NORTH_EAST, NORTH_WEST -> bounds.y += pointerSize;
            case EAST -> {
                bounds.x -= pointerSize;
                bounds.y += pointerSize / 2;
            }
            case WEST -> {
                bounds.y += pointerSize / 2;
                bounds.x += pointerSize;
            }
            case SOUTH, SOUTH_EAST, SOUTH_WEST -> bounds.height -= 2;
            default -> {
            }
        }
        return bounds;
    }

    @Override
    public void setBounds(final int x, final int y, final int width, final int height) {
        var r = adjustAlignment(new Rectangle(x, y, width, height));
        super.setBounds(r.x, r.y, r.width, r.height);
    }

    @Override
    public void showTooltip() {
        setVisible(true);
        startFadeTimer(0, 1, 0.05f);
    }

    @Override
    public void hideTooltip() {
        startFadeTimer(1, 0, -0.05f);
    }

    @Override
    public boolean isOptimized() {
        return optimized;
    }

    @Override
    public void setOptimized(boolean optimized) {
        this.optimized = optimized;
    }

    private void startFadeTimer(final float start, final float end, final float increment) {
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
                }
                repaint();
            }
        };
        timer.schedule(task, 0, 10);
    }

    @Override
    protected void paintBorder(final Graphics g) {
        final int pointerSize = bubbleBorder.getPointerSize();
        final int thickness = bubbleBorder.getThickness();
        final Border border = getBorder();
        //Move shadow according to alignment.
        Rectangle rect = switch (alignment) {
            case NORTH, NORTH_EAST, NORTH_WEST
                    -> new Rectangle(0, 0, getWidth(), getHeight() - pointerSize);
            case EAST -> new Rectangle(pointerSize, 0, getWidth() - pointerSize, getHeight());
            case SOUTH, SOUTH_EAST, SOUTH_WEST
                    -> new Rectangle(0, pointerSize, getWidth(), getHeight() - pointerSize);
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
