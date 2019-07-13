package edu.kit.mima;

import edu.kit.mima.gui.icons.SVGIcon;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Objects;
import java.util.Random;

/**
 * Mima Splash screen window.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class MimaSplash extends JWindow {

    private static final Composite COMPOSITE =
            AlphaComposite.getInstance(AlphaComposite.SRC_OVER).derive(0.5f);
    private final Icon image;
    private final Icon sum;
    private final JTextArea messageArea;
    private final Dimension splashDim;

    /**
     * Create new Mima Splash screen.
     *
     * @throws IOException if splash icons could not be loaded.
     */
    public MimaSplash() throws IOException {
        var dim = Toolkit.getDefaultToolkit().getScreenSize();
        splashDim = new Dimension((int) (dim.width / 2.5), (int) (dim.height / 2.5));
        int iw = splashDim.width / 4 - splashDim.width / 20;
        image = new SVGIcon(
                Objects.requireNonNull(App.class.getClassLoader().getResource("images/mima.svg")),
                iw,
                iw,
                true);
        sum = loadSum();
        messageArea = new JTextArea();
        messageArea.setOpaque(false);
        messageArea.setForeground(Color.WHITE);
        messageArea.setBounds(splashDim.width / 40,
                              splashDim.width / 20 + image.getIconHeight(),
                              splashDim.width / 4 - splashDim.width / 20,
                              splashDim.height - image.getIconHeight() - splashDim.width / 10);
        var layer = new JLayeredPane() {
            @Override
            public void paint(@NotNull final Graphics g) {
                paintBackground((Graphics2D) g.create());
                paintImage(g);
                paintMessage(g);
                paintLabel(g);
                g.setClip(getWidth() / 4, 0, 3 * getWidth() / 4, getHeight());
                paintSum(g);
            }
        };
        layer.add(messageArea);
        setLayeredPane(layer);
        setSize(splashDim);
    }

    private Icon loadSum() throws IOException {
        int size = Math.min(splashDim.width, splashDim.height);
        int index = new Random().nextInt(17) + 1;
        final URL url = App.class.getClassLoader().getResource("splash/sum-" + index + ".svg");
        return new SVGIcon(Objects.requireNonNull(url), size, size);
    }

    /**
     * Show the splash screen.
     */
    public void showSplash() {
        setAlwaysOnTop(true);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Close the splash screen.
     */
    public void closeSplash() {
        setVisible(false);
        dispose();
    }

    /**
     * Print a message to the splash screen.
     *
     * @param message the message to print.
     */
    public void showMessage(final String message) {
        messageArea.setText(message);
        repaint();
    }

    private void paintSum(final Graphics g) {
        int w = sum.getIconWidth();
        int h = sum.getIconHeight();
        int x = (getWidth() - w) / 2;
        int y = (getHeight() - h) / 2;
        sum.paintIcon(this, g, x, y);
    }

    private void paintImage(@NotNull final Graphics g) {
        int ix = getWidth() / 40;
        image.paintIcon(this, g, ix, ix);
    }

    private void paintMessage(@NotNull final Graphics g) {
        var g2 = g.create();
        g2.translate(messageArea.getX(), messageArea.getY());
        messageArea.paint(g2);
        g2.dispose();
    }

    private void paintLabel(@NotNull final Graphics g) {
        var font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        g.setColor(Color.WHITE);
        g.setFont(font);
        g.drawString(
                "Mima (" + Calendar.getInstance().get(Calendar.YEAR) + ")",
                getWidth() / 40,
                getHeight() - getWidth() / 40);
    }

    private void paintBackground(@NotNull final Graphics2D g) {
        var paint =
                new GradientPaint(
                        0, 0, new Color(72, 91, 126), getWidth(), getHeight(), new Color(134, 74, 128));
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.setPaint(paint);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setComposite(COMPOSITE);
        g.setColor(new Color(31, 31, 31));
        g.fillRect(0, 0, getWidth() / 4, getHeight());
        g.dispose();
    }
}
