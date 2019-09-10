package edu.kit.mima.gui.components.tabframe.popuptab;

import edu.kit.mima.gui.icon.Icons;
import com.weis.darklaf.components.alignment.Alignment;
import edu.kit.mima.gui.components.button.ClickAction;
import edu.kit.mima.gui.components.button.IconButton;
import edu.kit.mima.gui.components.tooltip.DefaultTooltipWindow;
import edu.kit.mima.gui.components.tooltip.TooltipUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.FocusManager;
import javax.swing.*;
import javax.swing.plaf.UIResource;
import java.awt.*;
import java.awt.event.AWTEventListener;

/**
 * @author Jannis Weis
 * @since 2019
 */
public abstract class SimplePopupComponent extends PopupComponent {

    protected final JButton closeButton;
    protected Color headerFocusBackground;
    protected Color headerBackground;

    public SimplePopupComponent() {
        closeButton = new PopupButton(Icons.COLLAPSE);
        var accelerator = "shift pressed ESCAPE";
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(accelerator), accelerator);
        getActionMap().put(accelerator, new ClickAction(closeButton));
        TooltipUtil.createDefaultTooltip(closeButton, new DefaultTooltipWindow("Hide (shift ESC)"));
        AWTEventListener eventListener = event -> {
            var owner = FocusManager.getCurrentManager().getFocusOwner();
            if ((owner instanceof JRootPane && (SwingUtilities.isDescendingFrom(SimplePopupComponent.this, owner)))
                || owner == null) {
                return;
            }
            setFocus(SwingUtilities.isDescendingFrom(owner, SimplePopupComponent.this));
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(eventListener, AWTEvent.FOCUS_EVENT_MASK);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        headerBackground = UIManager.getColor("TabFramePopup.background");
        headerFocusBackground = UIManager.getColor("TabFramePopup.focus");
    }

    @Override
    public void setCloseAction(final Action action) {
        closeButton.setAction(action);
    }

    @Override
    public void open() {
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

    private static final class PopupButton extends IconButton implements UIResource {

        private PopupButton(@NotNull final Icon icon) {
            super(icon);
        }
    }
}
