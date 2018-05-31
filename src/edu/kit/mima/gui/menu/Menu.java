package edu.kit.mima.gui.menu;

import javax.swing.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Menu extends JMenuBar {

    public Menu() {
        JMenu help = new JMenu("Help");
        JMenuItem show = new JMenuItem("Show Help");
        show.addActionListener(
                e -> {
                    System.out.print("pressed");
                    JFrame helpFrame = Help.getInstance();
                    helpFrame.setVisible(true);
                }
        );
        help.add(show);
        add(help);
    }
}
