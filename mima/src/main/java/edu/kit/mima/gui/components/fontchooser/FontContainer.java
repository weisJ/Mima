package edu.kit.mima.gui.components.fontchooser;

import org.jetbrains.annotations.Nullable;

import java.awt.Font;


/**
 * Created by dheid on 4/1/17.
 */
public interface FontContainer {

    String getSelectedStyle();

    float getSelectedSize();

    String getSelectedFamily();

    @Nullable Font getSelectedFont();

    void setSelectedFont(Font font);

    void setPreviewFont(Font font);

}
