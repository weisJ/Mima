package edu.kit.mima.gui.components.fontchooser;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

public class FontFamiliesFactory {

    public static FontFamilies create() {
        FontFamilies fontFamilies = new FontFamilies();
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] allFonts = graphicsEnvironment.getAllFonts();

        for (Font font : allFonts) {
            fontFamilies.add(font);
        }

        return fontFamilies;
    }

}
