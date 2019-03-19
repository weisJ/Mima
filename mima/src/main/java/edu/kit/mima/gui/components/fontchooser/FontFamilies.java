package edu.kit.mima.gui.components.fontchooser;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.Font;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class FontFamilies implements Iterable<FontFamily> {

    private static final FontFamilies INSTANCE = createFontFamilies();
    private final Map<String, FontFamily> families = new TreeMap<>();

    @NotNull
    private static FontFamilies createFontFamilies() {
        return FontFamiliesFactory.create();
    }

    @Contract(pure = true)
    @NotNull
    public static FontFamilies getInstance() {
        return INSTANCE;
    }

    /**
     * Add a font.
     *
     * @param font font to add.
     */
    public void add(@NotNull final Font font) {
        final String family = font.getFamily();
        final FontFamily fontFamily = families.computeIfAbsent(family, FontFamily::new);
        fontFamily.add(font);
    }

    @NotNull
    @Override
    public Iterator<FontFamily> iterator() {
        return families.values().iterator();
    }

    /**
     * Get font family from name.
     *
     * @param name name of font family.
     * @return font family with given name.
     */
    public FontFamily get(final String name) {
        return families.get(name);
    }
}
