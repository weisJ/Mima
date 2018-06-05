package edu.kit.mima.gui.menu;

import javax.swing.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class MenuBuilder {

    private JMenuBar menuBar;

    public MenuBuilder() {
        this.menuBar = new JMenuBar();
    }

    public SubMenuBuilder addMenu(String title) {
        return new SubMenuBuilder(title, this);
    }

    public class SubMenuBuilder {

        private JMenu menu;
        private MenuBuilder parent;

        private SubMenuBuilder(String title, MenuBuilder parent) {
            this.menu = new JMenu(title);
            this.parent = parent;
        }

        public SubMenuBuilder addItem(String title, Runnable action, String accelerator) {
            JMenuItem item = new JMenuItem(title);
            item.addActionListener(e -> action.run());
            item.setAccelerator(KeyStroke.getKeyStroke(accelerator));
            menu.add(item);
            return this;
        }

        public SubMenuBuilder addItem(String title, Runnable action) {
            JMenuItem item = new JMenuItem(title);
            item.addActionListener(e -> action.run());
            menu.add(item);
            return this;
        }

        public SubMenuBuilder addItem(String title) {
            JMenuItem item = new JMenuItem(title);
            menu.add(item);
            return this;
        }

        public SubMenuBuilder separator() {
            menu.addSeparator();
            return this;
        }

        public SubMenuBuilder addMenu(String title) {
            parent.menuBar.add(menu);
            return parent.addMenu(title);
        }

        public JMenuBar get() {
            parent.menuBar.add(menu);
            return parent.menuBar;
        }
    }
}
