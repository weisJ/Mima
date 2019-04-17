package edu.kit.mima.gui.components.popupmenu;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.MouseWheelEvent;
import java.lang.reflect.Field;


/**
 * Hacked JPopupMenu(Plus) - displayed in JScrollPane if too long.
 */
public class ScrollPopupMenu extends JPopupMenu {

    private final int maxHeight;
    @Nullable private JWindow popWin;
    @Nullable private JScrollPane scrollPane;
    private int posX;
    private int posY;
    private Border border;

    /**
     * Create Scroll Popup Menu.
     *
     * @param maxH maximum height.
     */
    public ScrollPopupMenu(final int maxH) {
        maxHeight = maxH;
        this.addMouseWheelListener(e -> {
            if (scrollPane != null) {
                for (final var wl : scrollPane.getMouseWheelListeners()) {
                    wl.mouseWheelMoved(e);
                }
            }
        });
    }

    /*
     * Prevent component from trying to close Popup.
     */
    private static void doNotCancelPopupHack(@NotNull final JComponent component) {
        try {
            final Class clazz = javax.swing.plaf.basic.BasicComboBoxUI.class;
            final Field field = clazz.getDeclaredField("HIDE_POPUP_KEY"); // NOI18N
            field.setAccessible(true);
            component.putClientProperty("doNotCancelPopup", field.get(null)); // NOI18N
        } catch (@NotNull final Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isVisible() {
        return popWin != null && popWin.isShowing();
    }

    @Override
    public void setVisible(final boolean visible) {
        if (visible == isVisible()) {
            return;
        }
        if (visible) {
            makeVisible();
        } else {
            makeHidden();
        }
    }

    private void makeVisible() {
        Component comp = getInvoker();
        if (comp == null) {
            return;
        }
        if (!(comp instanceof JMenu)) {
            MenuElement[] menuElements = new MenuElement[1];
            if (getSubElements().length > 0) {
                menuElements = new MenuElement[2];
                menuElements[1] = getSubElements()[0];
            }
            menuElements[0] = this;
            MenuSelectionManager.defaultManager().setSelectedPath(menuElements);
        }
        firePopupMenuWillBecomeVisible();

        while (comp.getParent() != null) {
            comp = comp.getParent();
        }
        popWin = comp instanceof Window
                ? new JWindow((Window) comp)
                : new JWindow(new JFrame());
        popWin.setLocation(posX, posY);
        pack();
        popWin.setVisible(true);
    }

    private void makeHidden() {
        getSelectionModel().clearSelection();
        if (popWin != null) {
            firePopupMenuWillBecomeInvisible();
            popWin.setVisible(false);
            popWin = null;
            scrollPane = null;
        }
    }

    @Override
    public void setLocation(final int x, final int y) {
        if (popWin != null && popWin.isShowing()) {
            popWin.setLocation(x, y);
        } else {
            posX = x;
            posY = y;
        }
    }

    @Override
    public void pack() {
        if (popWin == null) {
            return;
        }

        final Dimension prefSize = getPreferredSize();
        if (maxHeight == 0 || prefSize.height <= maxHeight) {
            if (scrollPane != null) {
                popWin.getContentPane().remove(scrollPane);
                scrollPane = null;
            }
            popWin.getContentPane().setLayout(null);
            popWin.getContentPane().add(this);
            setBounds(0, 0, prefSize.width, prefSize.height);
            popWin.setSize(prefSize.width, prefSize.height);
        } else {
            final int sbWidth = 8;
            if (scrollPane == null) {
                final JPanel view = new JPanel(new BorderLayout());
                view.add(this, BorderLayout.CENTER);

                scrollPane = new JScrollPane(view);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane.setBorder(border);
                super.setBorder(null);
                final JScrollBar bar = scrollPane.getVerticalScrollBar();
                if (bar != null) {
                    bar.putClientProperty("ScrollBar.thin", Boolean.TRUE);
                    final Dimension d = bar.getPreferredSize();
                    d.width = sbWidth;
                    bar.setPreferredSize(d);
                    int increment = 21;
                    if (getComponentCount() > 0) {
                        increment = Math.max(1, getComponent(0).getPreferredSize().height / 10);
                    }
                    bar.setUnitIncrement(increment);
                    doNotCancelPopupHack(bar);
                    scrollPane.addMouseWheelListener(e -> {
                        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                            bar.setValue(bar.getValue() + e.getUnitsToScroll());
                        }
                    });
                }
                popWin.getContentPane().add(scrollPane, BorderLayout.CENTER);
            }
            popWin.pack();
            popWin.setSize(popWin.getSize().width + sbWidth, maxHeight);
            requestFocus();
        }
    }

    /**
     * Get the scroll pane of the popup or null if it currently isn't needed.
     *
     * @return scroll pane;
     */
    @Nullable
    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    /**
     * Get the scrollbar of the popup or null if it currently is't needed.
     *
     * @return vertical scrollbar
     */
    @Nullable
    public JScrollBar getScrollBar() {
        return scrollPane != null ? scrollPane.getVerticalScrollBar() : null;
    }

    @Override
    public void setBorder(final Border border) {
        this.border = border;
        super.setBorder(border);
    }
}