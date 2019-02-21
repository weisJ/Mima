package edu.kit.mima.gui.editor;

import edu.kit.mima.core.parsing.lang.Punctuation;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * @author Jannis Weis
 * @since 2018
 */
public enum EditorHotKeys {

    COMMENT_TOGGLE("control 7") {
        @Override
        protected void onAction() {
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
        protected void onAction() {
            editor.redo();
        }
    },
    UNDO("control Z") {
        @Override
        protected void onAction() {
            editor.undo();
        }
    },
    ZOOM_IN("control PLUS") {
        @Override
        protected void onAction() {
            editor.setFontSize(editor.getFontSize() + 1);
        }
    },
    ZOOM_OUT("control MINUS") {
        @Override
        protected void onAction() {
            editor.setFontSize(editor.getFontSize() - 1);
        }
    };


    private static Editor editor;
    private static EventListener listener;

    private final String accelerator;

    EditorHotKeys(String accelerator) {
        this.accelerator = accelerator;
    }

    public static void setEditor(Editor editor) {
        EditorHotKeys.editor = editor;
    }

    public static KeyListener getKeyListener() {
        if (listener != null) {
            return listener;
        }
        listener = new EventListener();
        return listener;
    }

    protected abstract void onAction();

    private static class EventListener implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {
            System.out.println("KeyTyped " + e.paramString());
        }

        @Override
        public void keyPressed(KeyEvent e) {
            System.out.println("KeyPressed " + e.paramString());
        }

        @Override
        public void keyReleased(KeyEvent e) {
            System.out.println("KeyReleased " + e.paramString());
        }
    }

}
