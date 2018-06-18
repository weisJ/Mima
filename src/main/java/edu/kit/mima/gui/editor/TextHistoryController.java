package edu.kit.mima.gui.editor;

import edu.kit.mima.gui.editor.history.FileHistoryObject;
import edu.kit.mima.gui.editor.history.FileHistoryObject.ChangeType;
import edu.kit.mima.gui.editor.history.History;
import edu.kit.mima.gui.logging.Logger;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;

/**
 * Controls the change history of {@link Editor}
 *
 * @author Jannis Weis
 * @since 2018
 */
public class TextHistoryController {

    private static final int MAXIMUM_AMEND_LENGTH = 10;
    private final JTextPane editorPane;
    private final History<FileHistoryObject> history;
    private boolean active;

    /**
     * History Controller that controls the creation of HistoryObjects
     *
     * @param editorPane    editor pane to control
     * @param historyLength how many events the history should date back
     */
    public TextHistoryController(final JTextPane editorPane, final int historyLength) {
        this.editorPane = editorPane;
        history = new History<>(historyLength);
        active = true;
    }

    /**
     * Add History Object for Replace Event
     *
     * @param offset offset in document
     * @param length length of change
     * @param text   text inserted instead of old text
     */
    public void addReplaceHistory(final int offset, final int length, final String text) {
        if (!active) {
            return;
        }
        if (offset >= editorPane.getDocument().getLength()) {
            addInsertHistory(offset, text);
        } else {
            try {
                final String old = editorPane.getDocument().getText(offset, length);
                if (old.isEmpty()) {
                    addInsertHistory(offset, text);
                } else {
                    history.add(new FileHistoryObject(editorPane, offset, text, old, ChangeType.REPLACE));
                }
            } catch (final BadLocationException e) {
                Logger.error(e.getMessage());
            }
        }
    }

    /**
     * Add History Object for Insert Event
     *
     * @param offset offset in document
     * @param text   text inserted
     */
    public void addInsertHistory(final int offset, final String text) {
        if (!active) {
            return;
        }
        final FileHistoryObject fhs = history.getCurrent();
        if ((fhs != null)
                && (text.length() == 1)
                && !text.contains("\n") && !text.contains(" ")
                && (fhs.getType() == ChangeType.INSERT)
                && (offset == (fhs.getCaretOffset() + fhs.getText().length()))
                && (fhs.getText().length() < MAXIMUM_AMEND_LENGTH)) {
            history.setCurrent(
                    new FileHistoryObject(editorPane, fhs.getCaretOffset(), fhs.getText() + text, "",
                            ChangeType.INSERT));
        } else {
            history.add(new FileHistoryObject(editorPane, offset, text, "", ChangeType.INSERT));
        }
    }

    /**
     * Add History Object for Remove Event
     *
     * @param offset offset in document
     * @param length length of text removed
     */
    public void addRemoveHistory(final int offset, final int length) {
        if (!active) {
            return;
        }
        final FileHistoryObject fhs = history.getCurrent();
        String text = null;
        try {
            text = editorPane.getDocument().getText(offset, length);
        } catch (final BadLocationException e) {
            Logger.error(e.getMessage());
        }
        assert text != null;
        if ((fhs != null)
                && (text.length() == 1)
                && !text.contains("\n") && !text.contains(" ")
                && (fhs.getType() == ChangeType.REMOVE)
                && ((offset + length) == fhs.getCaretOffset())
                && (fhs.getOldText().length() < MAXIMUM_AMEND_LENGTH)) {
            history.setCurrent(
                    new FileHistoryObject(editorPane, offset, "", text + fhs.getOldText(),
                            ChangeType.REMOVE));
        } else {
            history.add(new FileHistoryObject(editorPane, offset, "", text, ChangeType.REMOVE));
        }
    }

    /**
     * Undo last file change
     */
    public void undo() {
        final FileHistoryObject fhs = history.back();
        final boolean isActive = active;
        setActive(false);
        fhs.undo();
        setActive(isActive);
    }

    /**
     * Redo the last undo
     */
    public void redo() {
        final FileHistoryObject fhs = history.forward();
        final boolean isActive = active;
        setActive(false);
        fhs.redo();
        setActive(isActive);
    }

    /**
     * Reset the History
     */
    public void reset() {
        history.reset();
    }

    /**
     * Set the history capacity
     *
     * @param capacity history capacity
     */
    public void setCapacity(final int capacity) {
        history.setCapacity(capacity);
    }

    /**
     * Returns whether the history controller is currently active
     *
     * @return true if active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Set whether the Controller should respond to changes in the document
     *
     * @param active true if it should respond
     */
    public void setActive(final boolean active) {
        this.active = active;
    }
}
