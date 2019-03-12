package edu.kit.mima.gui;

import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.gui.components.editor.Editor;
import edu.kit.mima.gui.formatter.Formatter;
import edu.kit.mima.gui.logging.Logger;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;

import javax.swing.Action;
import javax.swing.text.BadLocationException;
import java.awt.Font;
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
    STRING_CREATE("shift NUMBER_SIGN") {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                editor.insert(String.valueOf(Punctuation.STRING), editor.getCaretPosition());
                editor.setCaretPosition(editor.getCaretPosition() - 1);
            } catch (BadLocationException e1) {
                Logger.error(e1.getMessage());
            }
        }
    },
    STRING_DELETE("BACK_SPACE") {//Todo

        @Override
        public void actionPerformed(ActionEvent e) {
//            try {
//                String test = editor.getText(editor.getCaretPosition() - 1, 1);
//                System.out.println(test);
//            } catch (BadLocationException e1) {
//                Logger.error(e1.getMessage());
//            }
        }
    },
    FORMAT("control alt F") {
        private final Formatter formatter = new Formatter();

        @Override
        public void actionPerformed(ActionEvent e) {
//            String s = formatter.format(editor.getText());
            editor.setText(formatter.format(editor.getText()));
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
            Font font = pref.readFont(PropertyKey.EDITOR_FONT);
            Font newFont = font.deriveFont((float) font.getSize() + 1);
            pref.saveFont(PropertyKey.EDITOR_FONT, newFont);
        }
    },
    ZOOM_OUT("control MINUS") {
        @Override
        public void actionPerformed(ActionEvent e) {
            Font font = pref.readFont(PropertyKey.EDITOR_FONT);
            Font newFont = font.deriveFont((float) font.getSize() - 1);
            pref.saveFont(PropertyKey.EDITOR_FONT, newFont);
        }
    };


    private static final Preferences pref = Preferences.getInstance();
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
