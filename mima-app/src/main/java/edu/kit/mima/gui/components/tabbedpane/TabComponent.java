package edu.kit.mima.gui.components.tabbedpane;

import edu.kit.mima.gui.icon.Icons;
import edu.kit.mima.gui.components.IconLabel;
import edu.kit.mima.gui.components.button.IconButton;
import edu.kit.mima.gui.components.tooltip.DefaultTooltipWindow;
import edu.kit.mima.gui.components.tooltip.TooltipUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

/**
 * Component for tabs in {@link DnDTabbedPane}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class TabComponent extends JPanel {

    @NotNull
    private final IconLabel iconLabel;

    /**
     * Create new Tab Component.
     *
     * @param title   title of tab.
     * @param icon    the icon
     * @param onClick event handler when closing.
     */
    public TabComponent(final String title, @Nullable final Icon icon, @NotNull final Consumer<TabComponent> onClick) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        iconLabel = new IconLabel(icon, title, IconLabel.LEFT, 5, 3);
        iconLabel.setOpaque(false);
        add(iconLabel);
        add(Box.createRigidArea(new Dimension(5, 0)));
        add(Box.createGlue());

        var button = new IconButton(Icons.CLOSE, Icons.CLOSE_HOVER) {
            @Override
            protected Icon currentIcon() {
                return isHover() ? active : inactive;
            }

            @Override
            public void setEnabled(final boolean b) {
                setEnabledDirect(b);
            }
        };
        button.setRolloverEnabled(false);
        button.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                onClick.accept(TabComponent.this);
            }
        });
        TooltipUtil.createDefaultTooltip(button, new DefaultTooltipWindow("Close"));
        add(button);
        setBorder(new EmptyBorder(0, 0, 0, 5));
        setOpaque(false);
    }


    /**
     * Get the title.
     *
     * @return the title
     */
    public String getTitle() {
        return iconLabel.getTitle();
    }

    /**
     * Set the title for the tab component.
     *
     * @param title title to use.
     */
    public void setTitle(final String title) {
        iconLabel.setTitle(title);
    }

    /**
     * Get the icon.
     *
     * @return the icon
     */
    public Icon getIcon() {
        return iconLabel.getIcon();
    }
}
