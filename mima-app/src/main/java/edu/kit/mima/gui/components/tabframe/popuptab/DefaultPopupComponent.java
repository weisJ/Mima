package edu.kit.mima.gui.components.tabframe.popuptab;

import com.weis.darklaf.components.border.AdaptiveLineBorder;
import com.weis.darklaf.icons.EmptyIcon;
import edu.kit.mima.gui.components.IconLabel;
import com.weis.darklaf.components.alignment.Alignment;
import edu.kit.mima.gui.components.tabframe.TabFrame;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Default PopupComponent for {@link TabFrame}.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class DefaultPopupComponent extends SimplePopupComponent {

    @NotNull
    private final JPanel header;
    @NotNull
    private final JPanel content;
    private final JPanel headerCont;

    public DefaultPopupComponent(final String title, @NotNull final JComponent content) {
        this(title, EmptyIcon.create(0), content);
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
        header.add(closeButton);
        header.add(Box.createHorizontalStrut(1));
        header.setBorder(new EmptyBorder(1, 0, 1, 0));

        this.content = new JPanel(new BorderLayout());
        this.content.add(content, BorderLayout.CENTER);

        headerCont = new JPanel(new BorderLayout());
        headerCont.add(header, BorderLayout.CENTER);
        headerCont.setMinimumSize(new Dimension(0, 25));

        add(headerCont, BorderLayout.NORTH);
        add(this.content, BorderLayout.CENTER);
    }


    @Override
    public void setAlignment(@NotNull final Alignment a, final boolean[] info) {
        var insets = getBorderSize(a, info);
        headerCont.setBorder(new AdaptiveLineBorder(insets.top, insets.left, 1, insets.right,
                                                    "TabFramePopup.borderColor"));
        content.setBorder(new AdaptiveLineBorder(0, insets.left, insets.bottom, insets.right,
                                                 "TabFramePopup.borderColor"));
    }

    @Override
    public void setCloseAction(final Action action) {
        closeButton.setAction(action);
    }

    @Override
    public void open() {
        super.open();
        header.setBackground(headerFocusBackground);
    }

    @Override
    public void setFocus(final boolean focus) {
        if (focus) {
            header.setBackground(headerFocusBackground);
        } else {
            header.setBackground(headerBackground);
        }
        repaint();
    }
}
