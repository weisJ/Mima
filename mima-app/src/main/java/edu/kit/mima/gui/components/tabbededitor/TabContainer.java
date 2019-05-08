package edu.kit.mima.gui.components.tabbededitor;

import edu.kit.mima.gui.components.button.IconButton;
import edu.kit.mima.gui.components.listeners.PopupListener;
import edu.kit.mima.gui.components.popupmenu.ScrollPopupMenu;
import edu.kit.mima.gui.icons.Icons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.UIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

/**
 * Tab container for {@link EditorTabbedPane}.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class TabContainer extends JPanel implements UIResource {
    private final EditorTabbedPane tabPane;
    private final PopupListener listener;
    private final Stash stash;
    private boolean notifyTabbedPane;
    private int stashWidth;

    public TabContainer(final EditorTabbedPane tabPane) {
        super(null);
        setOpaque(false);
        this.tabPane = tabPane;
        stash = new Stash();
        listener = new PopupListener(null, MouseEvent.BUTTON1, true, true);
        stash.addMouseListener(listener);
        add(stash);
        notifyTabbedPane = true;
    }

    public void setNotifyTabbedPane(final boolean notifyTabbedPane) {
        this.notifyTabbedPane = notifyTabbedPane;
    }

    @Override
    public void remove(final Component comp) {
        int index = tabPane.indexOfTabComponent(comp);
        super.remove(comp);
        if (notifyTabbedPane && index != -1) {
            tabPane.setTabComponentAt(index, null);
        }
    }

    @Override
    public void doLayout() {
        // We layout tabComponents in JTabbedPane's layout manager
        // and use this method as a hook for repainting tabs
        // to update tabs area e.g. when the size of tabComponent was changed
        tabPane.repaint(getBounds());
    }

    public void removeUnusedTabComponents() {
        for (Component c : getComponents()) {
            if (!(c instanceof UIResource)) {
                int index = tabPane.indexOfTabComponent(c);
                if (index == -1) {
                    super.remove(c);
                }
            }
        }
    }

    @Override
    public void paint(@NotNull final Graphics g) {
        final var oldClip = new Rectangle(g.getClipBounds());
        var clip = new Rectangle(oldClip);
        clip.width -= stashWidth;
        g.setClip(clip);
        super.paint(g);
        if (stash.isVisible()) {
            g.setClip(oldClip);
            var b = stash.getBounds();
            g.translate(b.x, b.y);
            stash.paint(g);
        }
    }

    @Override
    public Dimension getMinimumSize() {
        var rect = tabPane.getBoundsAt(tabPane.getSelectedIndex());
        if (stash.isVisible()) {
            rect.width += stash.getPreferredSize().width;
            rect.width += 2 * (tabPane.getWidth() - stash.getX() - stash.getStashWidth());
        }
        return rect.getSize();
    }

    /**
     * Show the stash.
     *
     * @param minVisible minimum visible index.
     * @param maxVisible maximum visible index.
     */
    public void showStash(final int minVisible, final int maxVisible) {
        var menu = new ScrollPopupMenu(200);
        for (int i = 0; i < minVisible; i++) {
            var tc = (TabComponent) tabPane.getTabComponentAt(i);
            menu.add(createStashItem(i, tc));
        }
        for (int i = maxVisible + 1; i < tabPane.getTabCount(); i++) {
            var tc = (TabComponent) tabPane.getTabComponentAt(i);
            menu.add(createStashItem(i, tc));
        }
        int w = stash.getPreferredSize().width;
        stash.setBounds(getWidth() - stash.getStashWidth(), (getHeight() - w) / 2 + 1, w, w);
        listener.setPopupMenu(menu);
        stash.setVisible(true);
        stashWidth = stash.getStashWidth();
    }

    /**
     * Hide the stash.
     */
    public void hideStash() {
        stashWidth = 0;
        stash.setVisible(false);
    }

    /**
     * Get the stash.
     *
     * @return the stash.s
     */
    public Stash getStash() {
        return stash;
    }

    @NotNull
    private JMenuItem createStashItem(final int index, @NotNull final TabComponent c) {
        var item =
                new JMenuItem(
                        new AbstractAction() {
                            @Override
                            public void actionPerformed(final ActionEvent e) {
                                tabPane.setSelectedIndex(index);
                            }
                        });
        item.setText(c.getTitle());
        item.setIcon(c.getIcon());
        return item;
    }

    /**
     * Stash button.
     */
    public class Stash extends IconButton implements UIResource {
        private Stash() {
            super(Icons.MORE_TABS);
            setRolloverEnabled(false);
            setVisible(false);
        }

        public int getStashWidth() {
            return getPreferredSize().width + 2;
        }
    }
}
