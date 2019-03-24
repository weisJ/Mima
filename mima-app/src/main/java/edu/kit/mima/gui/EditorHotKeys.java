package edu.kit.mima.gui;

import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.formatter.Formatter;
import edu.kit.mima.formatter.MimaFormatter;
import edu.kit.mima.gui.components.editor.Editor;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.text.BadLocationException;

/**
 * Hot keys for {@link Editor}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public enum EditorHotKeys implements Action {

    COMMENT_TOGGLE("control 7") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            editor.transformLine(s -> {
                final String comment = String.valueOf(Punctuation.COMMENT);
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
        public void actionPerformed(final ActionEvent e) {
            try {
                editor.insert(String.valueOf(Punctuation.STRING), editor.getCaretPosition());
                editor.setCaretPosition(editor.getCaretPosition() - 1);
            } catch (@NotNull final BadLocationException ignored) {
            }
        }
    },
    STRING_DELETE("BACK_SPACE") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            //Todo
        }
    },
    FORMAT("control alt F") {
        private final Formatter formatter = new MimaFormatter();

        @Override
        public void actionPerformed(final ActionEvent e) {
            editor.setText(formatter.format(editor.getText()));
        }
    },
    REDO("control shift Z") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            editor.redo();
        }
    },
    UNDO("control Z") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            editor.undo();
        }
    },
    ZOOM_IN("control PLUS") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            changeFontSize(1);
        }
    },
    ZOOM_OUT("control MINUS") {
        @Override
        public void actionPerformed(final ActionEvent e) {
            changeFontSize(-1);
        }
    };


    private static final Preferences pref = Preferences.getInstance();
    private static Editor editor;
    private final String accelerator;

    @Contract(pure = true)
    EditorHotKeys(final String accelerator) {
        this.accelerator = accelerator;
    }

    public static void setEditor(final Editor editor) {
        EditorHotKeys.editor = editor;
    }

    protected void changeFontSize(int increment) {
        final Font font = pref.readFont(PropertyKey.EDITOR_FONT);
        final Font newFont = font.deriveFont((float) font.getSize() + increment);
        pref.saveFont(PropertyKey.EDITOR_FONT, newFont);
    }

    public String getAccelerator() {
        return accelerator;
    }

    @Override
    public abstract void actionPerformed(ActionEvent e);

    @Nullable
    @Override
    public Object getValue(final String key) {
        return null;
    }

    @Override
    public void putValue(final String key, final Object value) { }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void setEnabled(final boolean b) { }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener listener) { }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener listener) { }

}
