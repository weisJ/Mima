package edu.kit.mima.gui.components.popupMenu;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.lang.reflect.Field;


/**
 * Hacked JPopupMenu(Plus) - displayed in JScrollPane if too long.
 */
public class ScrollPopupMenu extends JPopupMenu {

    private JWindow popWin;
    private JScrollPane scrollPane;
    private int posX;
    private int posY;
    private int maxHeight;
    private Border border;

    public ScrollPopupMenu(int maxH) {
        maxHeight = maxH;
        this.addMouseWheelListener(e -> {
            if (scrollPane != null) {
                for (var wl : scrollPane.getMouseWheelListeners()) {
                    wl.mouseWheelMoved(e);
                }
            }
        });
    }

    /*
     * Prevent component from trying to close Popup.
     */
    private static void doNotCancelPopupHack(JComponent component) {
        try {
            Class clazz = javax.swing.plaf.basic.BasicComboBoxUI.class;
            Field field = clazz.getDeclaredField("HIDE_POPUP_KEY"); // NOI18N
            field.setAccessible(true);
            component.putClientProperty("doNotCancelPopup", field.get(null)); // NOI18N
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isVisible() {
        return popWin != null && popWin.isShowing();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible == isVisible())
            return;
        if (visible) {
            if (getInvoker() != null && !(getInvoker() instanceof JMenu)) {
                if (getSubElements().length > 0) {
                    MenuElement[] menuElements = new MenuElement[2];
                    menuElements[0] = this;
                    menuElements[1] = getSubElements()[0];
                    MenuSelectionManager.defaultManager().setSelectedPath(menuElements);
                } else {
                    MenuElement[] menuElements = new MenuElement[1];
                    menuElements[0] = this;
                    MenuSelectionManager.defaultManager().setSelectedPath(menuElements);
                }
            }

            firePopupMenuWillBecomeVisible();

            Component comp = getInvoker();
            while (comp.getParent() != null)
                comp = comp.getParent();

            popWin = comp instanceof Window ?
                    new JWindow((Window) comp) :
                    new JWindow(new JFrame());
            popWin.setLocation(posX, posY);

            pack();
            popWin.setVisible(true);
        } else {
            getSelectionModel().clearSelection();
            if (popWin != null) {
                firePopupMenuWillBecomeInvisible();
                popWin.setVisible(false);
                popWin = null;
                scrollPane = null;
            }
        }
    }

    @Override
    public void setLocation(int x, int y) {
        if (popWin != null && popWin.isShowing())
            popWin.setLocation(x, y);
        else {
            posX = x;
            posY = y;
        }
    }

    @Override
    public void pack() {
        if (popWin == null)
            return;

        Dimension prefSize = getPreferredSize();
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
            int sbWidth = 12;
            if (scrollPane == null) {
                JPanel view = new JPanel(new BorderLayout());
                view.add(this, BorderLayout.CENTER);

                scrollPane = new JScrollPane(view);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane.setBorder(border);
                super.setBorder(null);
                JScrollBar bar = scrollPane.getVerticalScrollBar();
                if (bar != null) {
                    Dimension d = bar.getPreferredSize();
                    d.width = sbWidth;
                    bar.setPreferredSize(d);
                    int increment = 21;
                    if (getComponentCount() > 0) {
                        increment = Math.max(1, getComponent(0).getPreferredSize().height / 3);
                    }
                    bar.setUnitIncrement(increment);
                    doNotCancelPopupHack(bar);
                }
                popWin.getContentPane().add(scrollPane, BorderLayout.CENTER);
            }
            popWin.pack();
            popWin.setSize(popWin.getSize().width + sbWidth, maxHeight);
            requestFocus();
        }
    }

    /**
     * Get the scroll pane of the popup or null if it currently
     * isn't needed.
     *
     * @return scroll pane;
     */
    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    /**
     * Get the scrollbar of the popup or null
     * if it currently is't needed.
     *
     * @return vertical scrollbar
     */
    public JScrollBar getScrollBar() {
        return scrollPane != null ? scrollPane.getVerticalScrollBar() : null;
    }

    @Override
    public void setBorder(Border border) {
        this.border = border;
        super.setBorder(border);
    }
}