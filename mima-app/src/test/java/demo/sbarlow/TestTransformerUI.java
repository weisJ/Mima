package demo.sbarlow;

import demo.TransformUtils;
import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.border.DropShadowBorder;
import org.jdesktop.swingx.painter.BusyPainter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Calendar;
import java.util.HashMap;

// import javax.swing.RepaintManager;

/**
 * A SwingX and TransformUI demo by <b>s_barlow</b>.
 * <p>
 * Run a web start demo: <a href="http://www.pbjar.org/blogs/jxlayer/jxlayer40/SBarlowDemo.jnlp">
 * <IMG style="CLEAR: right" alt="Web Start Shared JXLayer" src="http://javadesktop
 * .org/javanet_images/webstart.small2.gif"
 * align="middle" border="1" /> </a>
 * </p>
 */
public class TestTransformerUI extends JXFrame {

    private static final long serialVersionUID = 1L;

    public TestTransformerUI() {
        super("TestFrame", true);

        JXPanel contentPane = new JXPanel(new BorderLayout());
        contentPane.add(createTaskPanel(), BorderLayout.WEST);
        contentPane.add(createContent(), BorderLayout.CENTER);
        setContentPane(wrapLayer(contentPane));
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new TestTransformerUI().setVisible(true));
    }

    @NotNull
    private static Border createBorder(boolean active) {
        Color baseColor = Color.LIGHT_GRAY;
        Color shadowColor = Color.DARK_GRAY;
        Border inner = new LineBorder(active ? shadowColor : baseColor);
        Border outer = new DropShadowBorder(shadowColor, 20, .7f, 15, false, false, active, active);
        return new CompoundBorder(outer, inner);
    }

    @NotNull
    private static Color setAlpha(@NotNull Color color, double alpha) {
        int alphaInt = (int) (alpha * 255);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alphaInt);
    }

    private static void setUpBackground(@NotNull JXLabel comp, Color bg, boolean translucent) {
        Color bg1 = bg;
        if (translucent) {
            bg1 = setAlpha(bg1, 0.25);
        }
        comp.setBackground(bg1);
    }

    @NotNull
    private Container createContent() {
        JXPanel panel = new JXPanel(new GridLayout(3, 3));
        Component note;
        Color[] color =
                {Color.YELLOW, Color.ORANGE, Color.RED, Color.GREEN, Color.MAGENTA, Color.GRAY,
                 Color.LIGHT_GRAY, Color.PINK};
        for (int i = 1; i <= color.length; i++) {
            note = createNote("note number " + i, color[i - 1]);
            panel.add(note);
        }
        return panel;
    }

    @NotNull
    private Component createNote(String title, Color color) {
        JXPanel note = new JXPanel(new BorderLayout());
        note.setName(title);
        note.setOpaque(false);
        note.setBorder(createBorder(true));

        JXLabel header = new JXLabel();
        setUpBackground(header, color, false);
        header.setOpaque(true);
        header.setPreferredSize(new Dimension(0, 24));
        note.add(header, BorderLayout.NORTH);

        JXLabel label = new JXLabel("<html><i>" + title + "</i></html>");
        label.setBorder(new EmptyBorder(5, 10, 10, 10));
        label.setOpaque(true);
        setUpBackground(label, color, true);
        note.add(label, BorderLayout.CENTER);

        ReLocator listener = new ReLocator(note);

        note.addMouseMotionListener(listener);
        note.addMouseListener(listener);

        // label.addMouseMotionListener(listener); // PB
        // label.addMouseListener(listener); // PB

        return note;
    }

    @NotNull
    private Component createTaskPanel() {
        JXTaskPaneContainer container = new JXTaskPaneContainer();

        /*
         * some buttons
         */
        JXTaskPane task1 = new JXTaskPane();
        task1.setTitle("buttons");
        task1.setSpecial(true);

        JXButton button1 = new JXButton("first button");
        button1.addActionListener(e -> System.out.println("action 1"));
        task1.add(button1);

        JXButton button2 = new JXButton("2nd button");
        button2.addActionListener(e -> System.out.println("action 2"));
        task1.add(button2);
        container.add(task1);

        /*
         * a clock (or two)
         */
        JXTaskPane task2 = new JXTaskPane();
        task2.setTitle("the time");

        final JXLabel timeLabel = new JXLabel();
        timeLabel.setBorder(new LineBorder(Color.DARK_GRAY));
        timeLabel.setHorizontalAlignment(JXLabel.CENTER);
        task2.add(timeLabel);

        final JXBusyLabel busyLabel = new JXBusyLabel();
        busyLabel.setBorder(new LineBorder(Color.DARK_GRAY));
        busyLabel.setHorizontalAlignment(JXLabel.CENTER);
        busyLabel.setDelay(1000);

        /*
         * PB
         *
         * final BusyPainter<JXBusyLabel> busyPainter = new
         * BusyPainter<JXBusyLabel>();
         *
         * In SwingX 0.9.6 BusyPainter is not generic anymore.
         */
        final BusyPainter busyPainter = new BusyPainter();
        busyPainter.setDirection(JXBusyLabel.Direction.RIGHT);
        busyPainter.setBaseColor(setAlpha(Color.BLACK, 0.15f));
        busyPainter.setHighlightColor(Color.BLACK);
        busyPainter.setAntialiasing(true);
        busyPainter.setPaintCentered(true);
        busyPainter.setPoints(60);
        busyPainter.setTrailLength(15);
        busyPainter.setPointShape(new RoundRectangle2D.Float(2f, 2f, 10f, 1f, 2f, 2f));

        busyLabel.setBusyPainter(busyPainter);

        Dimension size = busyPainter.getTrajectory().getBounds().getSize();
        Rectangle shapeBounds = busyPainter.getPointShape().getBounds();
        size.height += (shapeBounds.width * 2 + shapeBounds.x * 2 + 4);

        busyLabel.setPreferredSize(size);
        busyLabel.setBusy(false);

        task2.add(busyLabel);
        container.add(task2);

        // update the clocks...
        Timer clock = new Timer(1, null);
        clock.setRepeats(true);
        clock.setDelay(1000);
        clock.addActionListener(new ActionListener() {
            @NotNull
            String timeFormat = "%02d:%02d:%02d";

            public void actionPerformed(ActionEvent e) {
                Calendar cal = Calendar.getInstance();
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int min = cal.get(Calendar.MINUTE);
                int sec = cal.get(Calendar.SECOND);
                int length = busyPainter.getPoints();

                int frame = (sec % length) - (length / 4);
                if (frame < 0) {
                    frame += length;
                }

                busyPainter.setFrame(frame);
                busyLabel.repaint();

                timeLabel.setText(String.format(timeFormat, hour, min, sec));
            }
        });
        clock.start();
        return container;
    }

    @NotNull
    private JXLayer<?> wrapLayer(JXPanel content) {
        HashMap<RenderingHints.Key, Object> hints = new HashMap<>();
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        /*
         * PB Wrap the content in a panel that displays the current
         * RepaintManager.
         */
        JXPanel debugPanel = new JXPanel(new BorderLayout()) {

            private static final long serialVersionUID = 1L;

            @Override
            public void paint(@NotNull Graphics g) {
                super.paint(g);
                // System.out.println(RepaintManager.currentManager(this)
                // .getClass().getName());
            }
        };
        debugPanel.add(content);

        //	TransformPort transformPort = new TransformPort(debugPanel);
        //	transformPort.setOpaque(true);
        //	transformPort.setBackground(Color.ORANGE);

        //	TransformUI zoomUI = new TransformUI();
        //	DefaultTransformModel transformer = new DefaultTransformModel();
        //	transformer.setPreferredScale(2.0);
        //	zoomUI.setModel(transformer);
        //	zoomUI.setRenderingHints(hints);

        return TransformUtils.createTransformJXLayer(debugPanel, 2.0, hints);

        //	return new JXLayer<TransformPort>(transformPort, zoomUI);
    }

    class ReLocator extends MouseAdapter {
        private final JXPanel myComp;
        @Nullable
        private Point offset;

        public ReLocator(JXPanel myComp) {
            this.myComp = myComp;
        }

        public void mouseClicked(MouseEvent e) {
            JXPanel panel = (JXPanel) myComp.getParent();

            float alpha = myComp.getAlpha();
            if (alpha < 1f) {
                alpha = 1f;
                panel.setComponentZOrder(myComp, 0); // moveToFront
                myComp.setBorder(createBorder(true));
            } else {
                alpha = 0.7f;
                panel.setComponentZOrder(myComp, panel.getComponentCount() - 1); // moveToBack
                myComp.setBorder(createBorder(false));
            }

            myComp.setAlpha(alpha);
            panel.revalidate();
            panel.repaint();
        }

        public void mousePressed(@NotNull MouseEvent e) {
            System.out.println("pressed: " + myComp.getName());
            offset = e.getPoint();
            offset.x = -offset.x;
            offset.y = -offset.y;
        }

        public void mouseReleased(MouseEvent e) {
            System.out.println("released: " + myComp.getName());
            offset = null;
        }

        public void mouseDragged(@NotNull MouseEvent e) {
            Dimension size = myComp.getSize();
            Dimension pSize = myComp.getParent().getSize();

            Point location = e.getPoint();
            location = SwingUtilities.convertPoint(myComp, location, myComp.getParent());
            location.translate(offset.x, offset.y);

            if (location.x < 0) {
                location.x = 0;
            } else if (location.x + size.width > pSize.width) {
                location.x = pSize.width - size.width;
            }
            if (location.y < 0) {
                location.y = 0;
            } else if (location.y + size.height > pSize.height) {
                location.y = pSize.height - size.height;
            }

            myComp.setLocation(location);
            myComp.setAlpha(1.0f);
            myComp.setBorder(createBorder(true));

            JXPanel panel = (JXPanel) myComp.getParent();
            panel.setComponentZOrder(myComp, 0); // moveToFront
        }
    }
}
