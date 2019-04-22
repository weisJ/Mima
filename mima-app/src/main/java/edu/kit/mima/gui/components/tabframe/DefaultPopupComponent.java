package edu.kit.mima.gui.components.tabframe;

import com.bulenkov.iconloader.util.EmptyIcon;
import edu.kit.mima.gui.components.IconLabel;
import edu.kit.mima.gui.components.alignment.Alignment;
import edu.kit.mima.gui.components.button.ClickAction;
import edu.kit.mima.gui.components.button.IconButton;
import edu.kit.mima.gui.components.tooltip.DefaultTooltipWindow;
import edu.kit.mima.gui.components.tooltip.TooltipUtil;
import edu.kit.mima.gui.icons.Icons;
import org.jetbrains.annotations.NotNull;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;

/**
 * Default PopupComponent for {@link TabFrame}.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class DefaultPopupComponent extends PopupComponent {

    @NotNull
    private final JPanel header;
    @NotNull
    private final JPanel content;
    @NotNull
    private final JButton closeButton;
    private Color headerFocusBackground;
    private Color headerBackground;
    private Color borderColor;
    private boolean open;
    private boolean locked = true;

    public DefaultPopupComponent(final String title, @NotNull final JComponent content) {
        this(title, new EmptyIcon(0, 0), content);
    }

    public DefaultPopupComponent(final String title, final Icon icon,
                                 @NotNull final JComponent content) {
        setLayout(new BorderLayout());

        header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        var label = new IconLabel(icon, title, IconLabel.LEFT, 8, 2);
        label.setOpaque(false);
        header.add(label);
        header.add(Box.createGlue());
        closeButton = new IconButton(Icons.COLLAPSE);
        header.add(closeButton);

        closeButton.addActionListener(e -> open = false);
        var accelerator = "shift pressed ESCAPE";
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(accelerator), accelerator);
        getActionMap().put(accelerator, new ClickAction(closeButton));
        TooltipUtil.createDefaultTooltip(closeButton, new DefaultTooltipWindow("Hide (shift ESC)"));

        this.content = new JPanel(new BorderLayout());
        this.content.add(content, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(this.content, BorderLayout.CENTER);

        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            private boolean pressed;

            @Override
            public void eventDispatched(@NotNull AWTEvent event) {
                if (locked) {
                    requestFocus();
                    locked = false;
                    return;
                }
                if (event.getID() == MouseEvent.MOUSE_CLICKED && open) {
                    if (pressed && mouseInside()) {
                        header.setBackground(headerFocusBackground);
                    } else {
                        header.setBackground(headerBackground);
                    }
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
        borderColor = UIManager.getColor("TabFramePopup.borderColor");
    }

    private boolean mouseInside() {
        var mousePos = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(mousePos, DefaultPopupComponent.this);
        return contains(mousePos);
    }

    public void setAlignment(@NotNull final Alignment a, final boolean[] info) {
        var insets = getBorderSize(a, info);
        header.setBorder(BorderFactory.createMatteBorder(insets.top, insets.left, 1, insets.right,
                                                         borderColor));
        content.setBorder(BorderFactory.createMatteBorder(0, insets.left, insets.bottom,
                                                          insets.right, borderColor));
    }

    @Override
    public void setCloseAction(Action action) {
        closeButton.setAction(action);
    }

    @Override
    public void open() {
        header.setBackground(headerFocusBackground);
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
    private Insets getBorderSize(@NotNull final Alignment a, final boolean[] info) {
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
