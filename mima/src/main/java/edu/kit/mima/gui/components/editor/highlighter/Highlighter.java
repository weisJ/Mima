package edu.kit.mima.gui.components.editor.highlighter;

import javax.swing.JTextPane;

/**
 * Highlighter for {@link edu.kit.mima.gui.components.editor.Editor}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface Highlighter {

    /**
     * Update Highlighting.
     *
     * @param pane TextPane to style
     */
    void updateHighlighting(JTextPane pane);
}
