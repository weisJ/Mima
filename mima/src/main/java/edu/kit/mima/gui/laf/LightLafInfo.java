package edu.kit.mima.gui.laf;

import javax.swing.UIManager;

/**
 * {@link javax.swing.UIManager.LookAndFeelInfo} for {@link CustomDarculaLightLaf}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class LightLafInfo extends UIManager.LookAndFeelInfo {
    /**
     * Constructs a {@link UIManager}s {@link javax.swing.UIManager.LookAndFeelInfo} object.
     */
    public LightLafInfo() {
        super("Light", CustomDarculaLightLaf.class.getCanonicalName());
    }
}
