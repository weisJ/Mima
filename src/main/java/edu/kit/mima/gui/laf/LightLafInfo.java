package edu.kit.mima.gui.laf;

import javax.swing.UIManager;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class LightLafInfo extends UIManager.LookAndFeelInfo {
    /**
     * Constructs a <code>UIManager</code>s
     * <code>LookAndFeelInfo</code> object.
     */
    public LightLafInfo() {
        super("Light", CustomDarculaLightLaf.class.getCanonicalName());
    }
}
