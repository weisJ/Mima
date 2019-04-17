import org.pbjar.jxlayer.plaf.ext.transform.TransformRPMFallBack;
import org.pbjar.jxlayer.plaf.ext.transform.TransformRPMImpl;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import java.awt.Graphics;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class RepaintManagerTest {

    private final Class<RepaintManager> rpmClass = RepaintManager.class;

    private final JMenu optionsMenu = new JMenu("Options");

    private final JCheckBoxMenuItem newRmItem = new JCheckBoxMenuItem(
            "new RepaintManager Hacked");

    private final JCheckBoxMenuItem newWrappedItem = new JCheckBoxMenuItem(
            "new WrappedRepaintManager (also hacked)");

    private final JCheckBoxMenuItem doubleBufItem = new JCheckBoxMenuItem(
            "DoubleBuffering off and on");

    private RepaintManager oldManager;

    public static void main(String[] args) {
        TransformRPMImpl.hack = true;
        SwingUtilities.invokeLater(() -> new RepaintManagerTest().createGui());
    }

    private void createGui() {

        optionsMenu.add(newWrappedItem);
        newWrappedItem.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            oldManager = RepaintManager.currentManager(null);
            RepaintManager newManager = new TransformRPMFallBack(
                    oldManager);
            RepaintManager.setCurrentManager(newManager);
            newWrappedItem.setEnabled(false);
        }));

        optionsMenu.add(newRmItem);
        newRmItem.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                oldManager = RepaintManager.currentManager(null);
                RepaintManager newManager;
                try {
                    /*
                     * Ensure that the new RepaintManager has the same
                     * bufferStrategyType as the original.
                     */
                    Field bufferStrategyType = rpmClass
                            .getDeclaredField("bufferStrategyType");
                    bufferStrategyType.setAccessible(true);
                    short strategyType = (Short) bufferStrategyType
                            .get(oldManager);
                    bufferStrategyType.setAccessible(false);
                    switch (strategyType) {
                        case (0) -> System.out
                                .println("bufferStrategyType: BUFFER_STRATEGY_NOT_SPECIFIED");
                        case (1) -> System.out
                                .println("bufferStrategyType: BUFFER_STRATEGY_SPECIFIED_ON");
                        case (2) -> System.out
                                .println("bufferStrategyType: BUFFER_STRATEGY_SPECIFIED_OFF");
                        default -> System.out.println("bufferStrategyType: "
                                                      + strategyType);
                    }
                    /*
                     * Construct a new RepaintManager with its private
                     * constructor and the same bufferStrategyType.
                     */
                    Constructor<RepaintManager> constructor = rpmClass
                            .getDeclaredConstructor(short.class);
                    constructor.setAccessible(true);
                    newManager = constructor.newInstance(strategyType);
                    constructor.setAccessible(false);
                    /*
                     * This is not enough. Now we need to copy the
                     * PaintManager from the original RepaintManager
                     * into the new RepaintManager.
                     */
                    Field paintManager = rpmClass
                            .getDeclaredField("paintManager");
                    paintManager.setAccessible(true);
                    Object paintManagerInstance = paintManager
                            .get(oldManager);
                    System.out
                            .println("PaintManager is of type: "
                                     + paintManagerInstance.getClass()
                                             .getName());
                    paintManager.set(newManager, paintManagerInstance);
                    paintManager.setAccessible(false);
                } catch (Throwable t) {
                    t.printStackTrace();
                    newManager = oldManager;
                }

                RepaintManager.setCurrentManager(newManager);
            });
            newRmItem.setEnabled(false);
        });

        optionsMenu.add(doubleBufItem);

        doubleBufItem.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                RepaintManager rm = RepaintManager.currentManager(null);
                rm.setDoubleBufferingEnabled(false);
                rm.setDoubleBufferingEnabled(true);
            });

            doubleBufItem.setEnabled(false);
        });

        optionsMenu.addSeparator();
        JMenuItem clearItem = new JMenuItem("Clear textArea");
        optionsMenu.add(clearItem);

        JMenuBar bar = new JMenuBar();
        bar.add(optionsMenu);

        final JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setFont(textArea.getFont().deriveFont(20f));

        clearItem.addActionListener(e -> textArea.setText(""));

        final JFrame frame = new JFrame("RM test") {

            private static final long serialVersionUID = 1L;

            public void paint(Graphics g) {
                super.paint(g);
                String newLine = System.currentTimeMillis()
                                 + " JFrame.paint()\n";
                textArea.setText(textArea.getText() + newLine);
            }
        };

        frame.add(new JScrollPane(textArea));
        frame.setJMenuBar(bar);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
