package edu.kit.mima.gui.components.fontchooser;

import org.jetbrains.annotations.NotNull;

import java.awt.Font;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class FontFamilies implements Iterable<FontFamily> {

    private static final FontFamilies INSTANCE = createFontFamilies();
    private final Map<String, FontFamily> families = new TreeMap<>();

    private static FontFamilies createFontFamilies() {
        return FontFamiliesFactory.create();
    }

    public static FontFamilies getInstance() {
        return INSTANCE;
    }

    public void add(Font font) {
        String family = font.getFamily();
        FontFamily fontFamily = families.computeIfAbsent(family, FontFamily::new);
        fontFamily.add(font);
    }

    @NotNull
    @Override
    public Iterator<FontFamily> iterator() {
        return families.values().iterator();
    }

    public FontFamily get(String name) {
        return families.get(name);
    }
}
