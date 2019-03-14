package edu.kit.mima.gui.laf;

import com.bulenkov.iconloader.util.SystemInfo;

import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class CustomDarculaLightLaf extends CustomDarculaLaf {

    public CustomDarculaLightLaf() {
        super();
        if (SystemInfo.isWindows || SystemInfo.isLinux) {
            base = new MetalLookAndFeel();
            MetalLookAndFeel.setCurrentTheme(new LightMetalTheme());
        }
    }

    @Override
    protected String getPrefix() {
        return "darcula_light";
    }
}
