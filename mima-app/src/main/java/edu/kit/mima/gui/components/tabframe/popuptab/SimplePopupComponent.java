package edu.kit.mima.gui.components.tabframe.popuptab;

import edu.kit.mima.gui.components.alignment.Alignment;
import edu.kit.mima.gui.components.button.ClickAction;
import edu.kit.mima.gui.components.button.IconButton;
import edu.kit.mima.gui.components.tooltip.DefaultTooltipWindow;
import edu.kit.mima.gui.components.tooltip.TooltipUtil;
import edu.kit.mima.gui.icons.Icons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;

/**
 * @author Jannis Weis
 * @since 2019
 */
public abstract class SimplePopupComponent extends PopupComponent {

    protected Color headerFocusBackground;
    protected Color headerBackground;
    protected final JButton closeButton;
    private boolean open;
    private boolean locked = true;

    public SimplePopupComponent() {
        closeButton = new IconButton(Icons.COLLAPSE);
        closeButton.addActionListener(e -> open = false);
        var accelerator = "shift pressed ESCAPE";
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(accelerator), accelerator);
        getActionMap().put(accelerator, new ClickAction(closeButton));
        TooltipUtil.createDefaultTooltip(closeButton, new DefaultTooltipWindow("Hide (shift ESC)"));

        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            private boolean pressed;

            @Override
            public void eventDispatched(@NotNull final AWTEvent event) {
                if (locked) {
                    requestFocus();
                    locked = false;
                    return;
                }
                if (event.getID() == MouseEvent.MOUSE_CLICKED && open) {
                    setFocus(pressed && mouseInside());
                    pressed = false;
                } else if (event.getID() == MouseEvent.MOUSE_PRESSED) {
                    pressed = mouseInside();
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        headerBackground = UIManager.getColor("TabFramePopup.background");
        headerFocusBackground = UIManager.getColor("TabFramePopup.focus");
    }

    private boolean mouseInside() {
        var mousePos = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(mousePos, SimplePopupComponent.this);
        return contains(mousePos);
    }

    @Override
    public void setCloseAction(final Action action) {
        closeButton.setAction(action);
    }

    @Override
    public void open() {
        open = true;
        /*
         * Lock first mouse event as the opening click would remove the focus
         * highlighting.
         */
        locked = true;
    }

    @Override
    public void close() {
        closeButton.doClick();
    }

    @NotNull
    protected Insets getBorderSize(@NotNull final Alignment a, final boolean[] info) {
        switch (a) {
            case NORTH, NORTH_EAST, SOUTH, SOUTH_WEST -> {
                var insets = new Insets(1, 0, 1, 0);
                if (a == Alignment.NORTH && info[Alignment.NORTH_EAST.getIndex()]) {
                    insets.right = 1;
                }
                if (a == Alignment.SOUTH_WEST && info[Alignment.SOUTH.getIndex()]) {
                    insets.right = 1;
                }
                if (a == Alignment.SOUTH || a == Alignment.SOUTH_WEST) {
                    insets.bottom = 0;
                }
                return insets;
            }
            case EAST, SOUTH_EAST -> {
                var insets = new Insets(1, 1, 0, 0);
                if ((info[Alignment.NORTH.getIndex()] || info[Alignment.NORTH_EAST.getIndex()])
                    && !(a == Alignment.SOUTH_EAST && info[Alignment.EAST.getIndex()])) {
                    insets.top = 0;
                }
                return insets;
            }
            case WEST, NORTH_WEST -> {
                var insets = new Insets(1, 0, 0, 1);
                if ((info[Alignment.NORTH.getIndex()] || info[Alignment.NORTH_EAST.getIndex()])
                    && !(a == Alignment.WEST && info[Alignment.NORTH_WEST.getIndex()])) {
                    insets.top = 0;
                }
                return insets;
            }
            default -> {
                return new Insets(0, 0, 0, 0);
            }
        }
    }
}
