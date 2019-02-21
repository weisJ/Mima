package edu.kit.mima.gui.editor.highlighter;

import edu.kit.mima.gui.editor.style.StyleGroup;

import java.util.Collection;

/**
 * @author Jannis Weis
 * @since 2018
 */
public interface Highlighter {

    /**
     * Get styleGroups of highlighter
     *
     * @return Collection of styleGroups
     */
    Collection<StyleGroup> getStyleGroups();

    /**
     * Update Highlighting.
     */
    void updateHighlighting();
}
