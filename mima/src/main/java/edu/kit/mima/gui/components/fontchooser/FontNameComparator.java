package edu.kit.mima.gui.components.fontchooser;

import java.awt.Font;
import java.util.Comparator;

public class FontNameComparator implements Comparator<Font> {

    @Override
    public int compare(Font o1, Font o2) {
        return o1.getName().compareTo(o2.getName());
    }

}
