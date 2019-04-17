package edu.kit.mima.gui.components.tabframe;

import com.bulenkov.iconloader.util.EmptyIcon;
import edu.kit.mima.gui.components.IconLabel;
import edu.kit.mima.gui.components.alignment.Alignment;
import edu.kit.mima.gui.components.button.IconButton;
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
import javax.swing.SwingUtilities;
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

    private final JPanel header;
    private final JComponent content;
    private final JButton closeButton;
    private final Color focusColor;
    private final Color color;
    private boolean open;
    private boolean locked = true;

    public DefaultPopupComponent(final String title, @NotNull final JComponent content) {
        this(title, new EmptyIcon(0, 0), content);
    }

    public DefaultPopupComponent(final String title, final Icon icon,
                                 @NotNull final JComponent content) {
        setLayout(new BorderLayout());
        this.content = content;

        header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        var label = new IconLabel(icon, title, IconLabel.LEFT, 8);
        label.setOpaque(false);
        header.add(label);
        header.add(Box.createGlue());
        closeButton = new IconButton(Icons.COLLAPSE);
        closeButton.addActionListener(e -> open = false);
        header.add(closeButton);

        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);

        color = header.getBackground();
        focusColor = new Color(59, 71, 84);
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            private boolean pressed;

            @Override
            public void eventDispatched(AWTEvent event) {
                if (locked) {
                    locked = false;
                    return;
                }
                if (event.getID() == MouseEvent.MOUSE_CLICKED && open) {
                    if (pressed && mouseInside()) {
                        header.setBackground(focusColor);
                    } else {
                        header.setBackground(color);
                    }
                    pressed = false;
                } else if (event.getID() == MouseEvent.MOUSE_PRESSED) {
                    pressed = mouseInside();
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
    }

    private boolean mouseInside() {
        var mousePos = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(mousePos, DefaultPopupComponent.this);
        return contains(mousePos);
    }

    public void setAlignment(final Alignment a, final boolean[] info) {
        var insets = getBorderSize(a, info);
        header.setBorder(BorderFactory.createMatteBorder(
                insets.top, insets.left, 1, insets.right,
                new Color(50, 50, 50)));
        content.setBorder(BorderFactory.createMatteBorder(
                0, insets.left, insets.bottom, insets.right,
                new Color(50, 50, 50)));
    }

    @Override
    public void setCloseAction(Action action) {
        closeButton.setAction(action);
    }

    @Override
    public void open() {
        header.setBackground(focusColor);
        open = true;
        /*
         * Lock first mouse event as the opening click would remove the focus
         * highlighting.
         */
        locked = true;
    }

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
