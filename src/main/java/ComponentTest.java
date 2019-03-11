import edu.kit.mima.gui.components.tooltip.Tooltip;
import edu.kit.mima.gui.components.tooltip.TooltipComponent;
import edu.kit.mima.gui.laf.CustomDarculaLaf;
import edu.kit.mima.gui.util.HSLColor;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Insets;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class ComponentTest {

    public static boolean print = false;
    public static boolean inside = false;

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(() -> {
            //Turn off metal's use of bold fonts
            try {
                UIManager.setLookAndFeel(CustomDarculaLaf.class.getCanonicalName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }
            UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
            UIManager.put("ToolTip.background", UIManager.getColor("TabbedPane.background"));
            UIManager.put("TabbedPane.tabsOverlapBorder", true);
            UIManager.put("TabbedPane.labelShift", 0);
            UIManager.put("TabbedPane.selectedLabelShift", 0);
            UIManager.put("TabbedPane.selectedTabPadInsets", new Insets(0, 0, 0, 0));
            UIManager.put("TabbedPane.tabAreaInsets", new Insets(0, 0, 0, 0));
            UIManager.put("TabbedPane.separaterHighlight", UIManager.getColor("TabbedPane.selected"));
            UIManager.put("TabbedPane.selected",
                    new HSLColor(UIManager.getColor("TabbedPane.background")).adjustTone(20));
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            var frame = new JFrame();
            frame.setSize(1000, 1000);
            frame.setLayout(new GridLayout(8, 8));
            frame.setGlassPane(new BetterGlassPane(frame));

            for (int i = 0; i < 64; i++) {
                JPanel button = new JPanel();
                button.setBorder(new LineBorder(Color.RED));
                new TooltipComponent<>(button,
                        new Tooltip("Hallo Laaaaaaaaaaaaaaaaaaaaaanger Text \n Test1 \n Tes 2 \n\n\n\nHoher Text"),
                        600, TooltipComponent.PERSISTENT, TooltipComponent.COMPONENT_BOTH);
                frame.add(button);
            }
            frame.setBackground(Color.WHITE);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }


}
