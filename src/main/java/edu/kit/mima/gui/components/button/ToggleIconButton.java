package edu.kit.mima.gui.components.button;

import javax.swing.Icon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class ToggleIconButton extends IconButton implements ActionListener {

    private boolean toggled;

    public ToggleIconButton(Icon icon) {
        this(icon, icon);
    }

    public ToggleIconButton(Icon inactive, Icon active) {
        super(inactive, active);
        this.inactive = inactive;
        this.active = active;
        addActionListener(this);
    }

    @Override
    protected boolean useAlternative() {
        return toggled;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        toggled = !toggled;
    }

    /**
     * Toggle the icon
     *
     * @param activeIcon whether to use the active icon
     */
    public void toggle(boolean activeIcon) {
        toggled = activeIcon;
    }
}
