package edu.kit.mima.gui.components.fontchooser;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class FontFamily implements Iterable<Font> {

    private final String name;

    private final Set<Font> styles = new TreeSet<>(new FontNameComparator());

    public FontFamily(final String name) {
        this.name = name;
    }

    public boolean add(final Font font) {
        return styles.add(font);
    }

    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public Iterator<Font> iterator() {
        return styles.iterator();
    }
}
