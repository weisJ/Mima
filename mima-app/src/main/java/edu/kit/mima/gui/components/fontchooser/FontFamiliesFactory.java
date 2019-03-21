package edu.kit.mima.gui.components.fontchooser;

import org.jetbrains.annotations.NotNull;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

public class FontFamiliesFactory {

    /**
     * Create the font families.
     *
     * @return Font Families.
     */
    @NotNull
    public static FontFamilies create() {
        final FontFamilies fontFamilies = new FontFamilies();
        final GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        final Font[] allFonts = graphicsEnvironment.getAllFonts();

        for (final Font font : allFonts) {
            fontFamilies.add(font);
        }

        return fontFamilies;
    }

}
