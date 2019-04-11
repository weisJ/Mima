import edu.kit.mima.gui.components.fontchooser.FontChooser;
import edu.kit.mima.gui.laf.LafManager;
import edu.kit.mima.gui.layout.WrapLayout;
import edu.kit.mima.gui.menu.settings.Settings;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class Test extends JScrollPane {

    int width = 0;

    public Test() {
        super();

        setAlignmentX(LEFT_ALIGNMENT);
        setAlignmentY(TOP_ALIGNMENT);

        final Box B = Box.createVerticalBox();
        B.setAlignmentX(LEFT_ALIGNMENT);
        B.setAlignmentY(TOP_ALIGNMENT);

        for (int i = 0; i < 4; i++) {
            B.add(new CPanel() {


                //Important!!! Make sure the width always fits the screen
                public Dimension getPreferredSize() {


                    Dimension result = super.getPreferredSize();
                    result.width = width - 20; // 20 is for the scroll bar width
                    return result;
                }
            });
        }

        setViewportView(B);

        //Important!!! Need to invalidate the Scroll pane, othewise it
        //doesn't try to lay out when the container is shrunk
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent ce) {
                width = getWidth();
                B.invalidate();
            }
        });
    }

    public static String getRandomMultilineText() {
        String filler = "";
        int words = (int) (Math.random() * 7) + 1;
        for (int w = 0; w < words; w++) {
            int lettersInWord = (int) (Math.random() * 12) + 1;
            for (int l = 0; l < lettersInWord; l++) {
                filler += "a";
            }
            filler += "\n";
        }
        return filler.trim();
    }

    public static void main(String[] args) {
        LafManager.setDefaultTheme(true);
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new FontChooser());
        frame.pack();
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        Settings.showWindow(frame);
    }

    // nothing really very special in this class - mostly here for demonstration
    public static class CPanel extends JPanel {

        public CPanel() {
            super(new WrapLayout(WrapLayout.LEFT));
            ((WrapLayout) getLayout()).setAlignOnBaseline(true);


            setOpaque(true);
            setBackground(Color.gray);
            setAlignmentY(TOP_ALIGNMENT);
            setAlignmentX(LEFT_ALIGNMENT);


            int wordGroups = (int) (Math.random() * 14) + 7;

            //Adding test data (TextAreas)
            for (int i = 0; i < wordGroups; i++) {

                JTextArea ta = new JTextArea(getRandomMultilineText());
                ta.setAlignmentY(TOP_ALIGNMENT);
                ta.setAlignmentX(LEFT_ALIGNMENT);
                add(ta);
            }
            Border bx = BorderFactory.createTitledBorder("Lovely container");

            setBorder(bx);
        }
    }
}