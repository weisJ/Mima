package edu.kit.mima.gui.editor;


import java.awt.*;
import java.util.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class StyleGroup {

    Map<String, Color> styleMap;

    public StyleGroup() {
        styleMap = new HashMap<>();
    }

    public Set<String> regexSet() {
        return styleMap.keySet();
    }

    public Color getColor(String key) {
        return styleMap.get(key);
    }

    public void addHighlight(String[] regexArray, Color color) {
        if (regexArray.length == 0) return;
        StringBuilder sb = new StringBuilder("(");
        for (String s : regexArray) {
            sb.append(s).append("|");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        addHighlight(sb.toString(), color);
    }

    public void addHighlight(String regex, Color color) {
        styleMap.put(regex, color);
    }

    public void addHighlight(String[] regexArray, Color[] colors) {
        if (regexArray.length == 0) return;
        if (regexArray.length != colors.length) {
            throw new IllegalArgumentException("unequal array lengths");
        }
        for (int i = 0; i < regexArray.length; i++) {
            addHighlight(regexArray[i], colors[i]);
        }
    }

    public void setHighlight(String[] regexArray, Color color) {
        styleMap = new HashMap<>();
        addHighlight(regexArray, color);
    }

    public void setHighlight(String[] regexArray, Color[] colors) {
        if (regexArray.length == 0) return;
        styleMap = new HashMap<>();
        addHighlight(regexArray, colors);
    }

    public void setHighlight(String regex, Color color) {
        styleMap = new HashMap<>();
        addHighlight(regex, color);
    }
}
