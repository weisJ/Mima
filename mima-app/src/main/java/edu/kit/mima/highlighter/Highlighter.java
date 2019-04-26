package edu.kit.mima.highlighter;

import edu.kit.mima.api.history.FileHistoryObject;

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
     * @param fhs  history change object
     */
    void updateHighlighting(JTextPane pane, FileHistoryObject fhs);
}
