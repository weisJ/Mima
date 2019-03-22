package edu.kit.mima.gui.laf;

import com.bulenkov.iconloader.util.SystemInfo;
import org.jetbrains.annotations.NotNull;

import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * Extension of {@link CustomDarculaLaf} to light colours.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class CustomDarculaLightLaf extends CustomDarculaLaf {

    /**
     * Create new light Darcula LaF.
     */
    public CustomDarculaLightLaf() {
        super();
        if (SystemInfo.isWindows || SystemInfo.isLinux) {
            MetalLookAndFeel.setCurrentTheme(new LightMetalTheme());
        }
    }

    @NotNull
    @Override
    protected String getPrefix() {
        return "darcula_light";
    }
}
