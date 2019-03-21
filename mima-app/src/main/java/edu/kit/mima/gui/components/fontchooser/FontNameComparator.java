package edu.kit.mima.gui.components.fontchooser;

import org.jetbrains.annotations.NotNull;

import java.awt.Font;
import java.util.Comparator;

public class FontNameComparator implements Comparator<Font> {

    @Override
    public int compare(@NotNull final Font o1, @NotNull final Font o2) {
        return o1.getName().compareTo(o2.getName());
    }

}
