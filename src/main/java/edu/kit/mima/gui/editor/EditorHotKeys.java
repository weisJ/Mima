package edu.kit.mima.gui.editor;

import edu.kit.mima.core.parsing.lang.Punctuation;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Jannis Weis
 * @since 2018
 */
public enum EditorHotKeys implements Action {

    COMMENT_TOGGLE("control 7") {
        @Override
        public void actionPerformed(ActionEvent e) {
            editor.transformLine(s -> {
                String comment = String.valueOf(Punctuation.COMMENT);
                if (s.trim().startsWith(comment)) {
                    return s.replaceFirst(comment, "");
                } else {
                    return comment + s;
                }
            }, editor.getCaretPosition());
        }
    },
    REDO("control shift Z") {
        @Override
        public void actionPerformed(ActionEvent e) {
            editor.redo();
        }
    },
    UNDO("control Z") {
        @Override
        public void actionPerformed(ActionEvent e) {
            editor.undo();
        }
    },
    ZOOM_IN("control PLUS") {
        @Override
        public void actionPerformed(ActionEvent e) {
            editor.setFontSize(editor.getFontSize() + 1);
        }
    },
    ZOOM_OUT("control MINUS") {
        @Override
        public void actionPerformed(ActionEvent e) {
            editor.setFontSize(editor.getFontSize() - 1);
        }
    };


    private static Editor editor;
    private final String accelerator;

    EditorHotKeys(String accelerator) {
        this.accelerator = accelerator;
    }

    public static void setEditor(Editor editor) {
        EditorHotKeys.editor = editor;
    }

    public String getAccelerator() {
        return accelerator;
    }

    @Override
    public abstract void actionPerformed(ActionEvent e);

    @Override
    public Object getValue(String key) { return null; }

    @Override
    public void putValue(String key, Object value) { }

    @Override
    public boolean isEnabled() { return true; }

    @Override
    public void setEnabled(boolean b) { }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) { }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) { }

}
