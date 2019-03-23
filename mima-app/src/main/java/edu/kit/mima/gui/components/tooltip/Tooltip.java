package edu.kit.mima.gui.components.tooltip;

import edu.kit.mima.gui.components.Alignment;
import edu.kit.mima.gui.components.ShadowPane;
import edu.kit.mima.gui.components.TextBubbleBorder;
import edu.kit.mima.util.HSLColor;
import org.jetbrains.annotations.NotNull;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * Tooltip Component with Shadow.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class Tooltip extends ShadowPane implements ITooltip {
    private final TextBubbleBorder bubbleBorder;
    private final JLabel textLabel;
    private Alignment alignment;
    private float alpha = 0;

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

        bubbleBorder = new TextBubbleBorder(
                new HSLColor(textLabel.getBackground()).adjustTone(60)
                        .getRGB()).setPointerSize(5).setThickness(1);

        final JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.setOpaque(false);
        labelPanel.add(textLabel, BorderLayout.CENTER);
        labelPanel.setBorder(bubbleBorder);
        labelPanel.setBackground(new HSLColor(labelPanel.getBackground()).adjustTone(20).getRGB());

        add(labelPanel);
    }

    /**
     * Set the display text for the Tooltip.
     *
     * @param text tooltip text
     */
    public void setText(@NotNull final String text) {
        textLabel.setText("<html>" + text.replaceAll("\n", "<\\br>") + "</html>");
    }

    @Override
    public Alignment getAlignment() {
        return alignment;
    }

    @Override
    public void setAlignment(@NotNull final Alignment alignment) {
        this.alignment = alignment;
        adjustAlignment(alignment);
        bubbleBorder.setPointerSide(alignment.opposite());
    }

    /*
     * Apply insets to adjust position
     */
    private void adjustAlignment(@NotNull final Alignment alignment) {
        final Rectangle bounds = getBounds();
        final Insets insets = getInsets();
        switch (alignment) {
            case NORTH:
                setBounds(bounds.x, bounds.y + insets.bottom,
                          bounds.width, bounds.height);
                break;
            case EAST:
                setBounds(bounds.x - insets.left, bounds.y + insets.bottom / 2,
                          bounds.width + insets.right / 2, bounds.height);
                break;
            case SOUTH:
                setBounds(bounds.x, bounds.y + insets.top,
                          bounds.width, bounds.height);
                break;
            case WEST:
                setBounds(bounds.x + insets.left / 2, bounds.y + insets.bottom / 2,
                          bounds.width + insets.left / 2, bounds.height);
                break;
            case NORTH_EAST:
                setBounds(bounds.x - insets.left - insets.right,
                          bounds.y + insets.bottom,
                          bounds.width, bounds.height);
                bubbleBorder.setPointerPadPercent(0);
                break;
            case SOUTH_EAST:
                setBounds(bounds.x - insets.right - insets.left,
                          bounds.y, bounds.width, bounds.height);
                bubbleBorder.setPointerPadPercent(0);
                break;
            case NORTH_WEST:
                setBounds(bounds.x + insets.right + insets.left,
                          bounds.y + insets.bottom, bounds.width, bounds.height);
                bubbleBorder.setPointerPadPercent(1);
                break;
            case SOUTH_WEST:
                setBounds(bounds.x + insets.right + insets.left,
                          bounds.y, bounds.width, bounds.height);
                bubbleBorder.setPointerPadPercent(1);
                break;
            case CENTER:
            default:
                break;
        }
    }

    @Override
    public void showTooltip() {
        setVisible(true);
        startFadeTimer(true);
    }

    @Override
    public void hideTooltip() {
        startFadeTimer(false);
    }

    private void startFadeTimer(final boolean fadeIn) {
        final float end = fadeIn ? 1 : 0;
        alpha = fadeIn ? 0 : 1;
        final var timer = new Timer();
        final var task = new TimerTask() {
            @Override
            public void run() {
                if (alpha == end) {
                    timer.cancel();
                    if (alpha == 0) {
                        setVisible(false);
                    }
                }
                alpha = fadeIn ? (float) Math.min(alpha + 0.075, 1)
                        : (float) Math.max(alpha - 0.075, 0);
                repaint();
            }
        };
        timer.schedule(task, 0, 10);
    }

    @Override
    protected void paintBorder(final Graphics g) {
        final int pointerSize = bubbleBorder.getPointerSize();
        final Border border = getBorder();
        //Move shadow according to alignment.
        switch (alignment) {
            case NORTH:
            case NORTH_EAST:
            case NORTH_WEST:
                border.paintBorder(this, g, 0, 0, getWidth(), getHeight() - pointerSize);
                break;
            case EAST:
                border.paintBorder(this, g, pointerSize, 0, getWidth() - pointerSize, getHeight());
                break;
            case SOUTH:
                border.paintBorder(this, g, 0, pointerSize, getWidth(), getHeight() - pointerSize);
                break;
            case CENTER:
            case SOUTH_EAST:
            case SOUTH_WEST:
                border.paintBorder(this, g, 0, 0, getWidth(), getHeight());
                break;
            case WEST:
                border.paintBorder(this, g, 0, 0, getWidth() - pointerSize, getHeight());
                break;
            default:
                break;
        }
    }

    @Override
    public void paint(@NotNull final Graphics g) {
        final Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        super.paint(g2d);
        g2d.dispose();

    }
}
