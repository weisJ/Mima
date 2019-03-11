import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.AWTEvent;
import java.awt.Point;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;

/**
 * GlassPane tutorial
 * "A well-behaved GlassPane"
 * http://weblogs.java.net/blog/alexfromsun/
 * <p/>
 * This is a much better version of GlassPane
 * because it is really transparent for MouseEvents,
 * the only drawback that it doesn't update the mouse's cursor
 * for the components underneath
 * (e.g. doesn't show TEXT_CURSOR for textComponents)
 *
 * @author Alexander Potochkin
 */

public class BetterGlassPane extends JPanel implements AWTEventListener {
    private final JFrame frame;
    private Point point = new Point();

    public BetterGlassPane(JFrame frame) {
        super(null);
        this.frame = frame;
        setOpaque(false);
    }

    public void setPoint(Point point) {
        this.point = point;
    }

//    protected void paintComponent(Graphics g) {
//        Graphics2D g2 = (Graphics2D) g;
//        g2.setColor(Color.ORANGE);
//        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
//        int d = 22;
//        g2.fillRect(getWidth() - d, 0, d, d);
//        if (point != null) {
//            g2.fillOval(point.x + d, point.y + d, d, d);
//        }
//        g2.dispose();
//        super.paintComponent(g);
//    }

    public void eventDispatched(AWTEvent event) {
        if (event instanceof MouseEvent) {
            MouseEvent me = (MouseEvent) event;
            if (!SwingUtilities.isDescendingFrom(me.getComponent(), frame)) {
                return;
            }
            if (me.getID() == MouseEvent.MOUSE_EXITED && me.getComponent() == frame) {
                point = null;
            } else {
                MouseEvent converted = SwingUtilities.convertMouseEvent(me.getComponent(), me, frame.getGlassPane());
                point = converted.getPoint();
            }
            repaint();
        }
    }
} 