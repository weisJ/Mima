package edu.kit.mima.gui.components.tooltip;

import edu.kit.mima.gui.components.Alignment;
import edu.kit.mima.gui.components.ShadowPane;
import edu.kit.mima.gui.components.TextBubbleBorder;
import edu.kit.mima.gui.util.HSLColor;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Tooltip extends ShadowPane implements ITooltip {
    private TextBubbleBorder bubbleBorder;
    private JLabel textLabel;
    private Alignment alignment;
    private float alpha = 0;

    public Tooltip(String text) {
        alignment = Alignment.NORTH;
        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.setOpaque(false);
        textLabel = new JLabel();
        setText(text);
        textLabel.setOpaque(false);
        textLabel.setBorder(new EmptyBorder(2, 5, 5, 5));
        labelPanel.add(textLabel, BorderLayout.CENTER);
        bubbleBorder = new TextBubbleBorder(
                new HSLColor(labelPanel.getBackground()).adjustTone(60).getRGB()).setPointerSize(5).setThickness(1);
        labelPanel.setBorder(bubbleBorder);
        add(labelPanel);
        //Prevent events from propagating to components beneath
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                e.consume();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                e.consume();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                e.consume();
            }
        });
    }

    /**
     * Set the display text for the Tooltip.
     *
     * @param text tooltip text
     */
    public void setText(String text) {
        textLabel.setText("<html>" + text.replaceAll("\n", "<\\br>") + "</html>");
    }

    @Override
    public Alignment getAlignment() {
        return alignment;
    }

    @Override
    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
//        switch(alignment) {
//            case NORTH:
//                bubbleBorder.setPointerSide(Alignment.SOUTH);
//                break;
//            case NORTH_EAST:
//                bubbleBorder.setPointerSide(Alignment.SOUTH_WEST);
//                break;
//            case EAST:
//                bubbleBorder.setPointerSide(Alignment.WEST);
//                break;
//            case SOUTH_EAST:
//                bubbleBorder.setPointerSide(Alignment.NORTH_WEST);
//                break;
//            case SOUTH:
//                bubbleBorder.setPointerSide(Alignment.NORTH);
//                break;
//            case SOUTH_WEST:
//                bubbleBorder.setPointerSide(Alignment.NORTH_EAST);
//                break;
//            case WEST:
//                bubbleBorder.setPointerSide(Alignment.EAST);
//                break;
//            case NORTH_WEST:
//                bubbleBorder.setPointerSide(Alignment.SOUTH_EAST);
//                break;
//            case CENTER:
//                bubbleBorder.setPointerSide(Alignment.CENTER);
//                break;
//        }
        bubbleBorder.setPointerSide(alignment.opposite());
    }

    @Override
    public void showTooltip() {
        alpha = 0;
        var timer = new Timer();
        var task = new TimerTask() {
            @Override
            public void run() {
                if (alpha == 1) {
                    timer.cancel();
                }
                alpha = (float) Math.min(alpha + 0.075, 1);
                repaint();
            }
        };
        timer.schedule(task, 0, 10);
    }

    @Override
    protected void paintBorder(Graphics g) {
        int pointerSize = bubbleBorder.getPointerSize();
        Border border = getBorder();
        switch (alignment) {
            case NORTH:
                border.paintBorder(this, g, 0, 0, getWidth(), getHeight() - pointerSize);
                break;
            case NORTH_EAST:
                super.paintBorder(g);
                break;
            case EAST:
                border.paintBorder(this, g, pointerSize, 0, getWidth() - pointerSize, getHeight());
                break;
            case SOUTH_EAST:
                super.paintBorder(g);
                break;
            case SOUTH:
                border.paintBorder(this, g, 0, pointerSize, getWidth(), getHeight() - pointerSize);
                break;
            case SOUTH_WEST:
                super.paintBorder(g);
                break;
            case WEST:
                border.paintBorder(this, g, 0, 0, getWidth() - pointerSize, getHeight());
                break;
            case NORTH_WEST:
                super.paintBorder(g);
                break;
            case CENTER:
                super.paintBorder(g);
                break;
        }
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        super.paint(g2d);
        g2d.dispose();

    }
}
