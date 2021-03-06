package edu.kit.mima.gui.components.text.editor;

import edu.kit.mima.api.history.EditorHistory;
import edu.kit.mima.api.history.FileHistoryObject;
import edu.kit.mima.api.history.FileHistoryObject.ChangeType;
import edu.kit.mima.api.history.History;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

/**
 * Controls the change history of {@link Editor}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class TextHistoryController {

    private static final int MAXIMUM_AMEND_LENGTH = 10;
    private final JTextComponent textComponent;
    @NotNull
    private final History<FileHistoryObject> history;
    private boolean active;

    /**
     * History Controller that controls the creation of HistoryObjects.
     *
     * @param textComponent editor pane to control
     * @param historyLength how many events the history should date back
     */
    @Contract(pure = true)
    public TextHistoryController(final JTextComponent textComponent, final int historyLength) {
        this.textComponent = textComponent;
        history = new EditorHistory(historyLength);
        active = true;
    }

    /**
     * Add History Object for Replace Event.
     *
     * @param offset offset in document
     * @param length length of change
     * @param text   text inserted instead of old text
     * @throws BadLocationException if offset or offset + length is not inside the document bounds.
     */
    public void addReplaceHistory(final int offset, final int length, @NotNull final String text)
            throws BadLocationException {
        if (!active) {
            return;
        }
        if (offset >= textComponent.getDocument().getLength()) {
            addInsertHistory(offset, text);
        } else {
            final String old = textComponent.getDocument().getText(offset, length);
            if (old.isEmpty()) {
                addInsertHistory(offset, text);
            } else {
                history.addAtHead(new FileHistoryObject(textComponent, offset, text, old, ChangeType.REPLACE));
            }
        }
    }

    /**
     * Add History Object for Insert Event.
     *
     * @param offset offset in document
     * @param text   text inserted
     */
    public void addInsertHistory(final int offset, @NotNull final String text) {
        if (!active) {
            return;
        }
        final FileHistoryObject fhs = history.getCurrent();
        if ((fhs != null)
            && useSingle(text, fhs)
            && (fhs.getType() == ChangeType.INSERT)
            && (offset == (fhs.getCaretOffset() + fhs.getText().length()))) {
            history.setCurrent(
                    new FileHistoryObject(
                            textComponent, fhs.getCaretOffset(), fhs.getText() + text, "", ChangeType.INSERT));
        } else {
            history.addAtHead(new FileHistoryObject(textComponent, offset, text, "", ChangeType.INSERT));
        }
    }

    private boolean useSingle(@NotNull final String text, @NotNull final FileHistoryObject fhs) {
        return (text.length() == 1)
               && !text.contains("\n")
               && !text.contains(" ")
               && (fhs.getText().length() < MAXIMUM_AMEND_LENGTH);
    }

    /**
     * Add History Object for Remove Event.
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
            text = textComponent.getDocument().getText(offset, length);
        } catch (@NotNull final BadLocationException e) {
            e.printStackTrace();
        }
        assert text != null;
        if ((fhs != null)
            && useSingle(text, fhs)
            && (fhs.getType() == ChangeType.REMOVE)
            && ((offset + length) == fhs.getCaretOffset())) {
            history.setCurrent(
                    new FileHistoryObject(
                            textComponent, offset, "", text + fhs.getOldText(), ChangeType.REMOVE));
        } else {
            history.addAtHead(new FileHistoryObject(textComponent, offset, "", text, ChangeType.REMOVE));
        }
    }

    /**
     * Undo last file change.
     */
    public void undo() {
        walkHistory(true);
    }

    /**
     * Redo the last undo.
     */
    public void redo() {
        walkHistory(false);
    }

    /**
     * Walk in history forward or backwards.
     *
     * @param undo true if undo false if redo.
     */
    private void walkHistory(final boolean undo) {
        if (undo ? !canUndo() : !canRedo()) {
            return;
        }
        try {
            final FileHistoryObject fhs = undo ? history.back() : history.forward();
            final boolean isActive = active;
            setActive(false);
            if (undo) {
                fhs.undo();
            } else {
                fhs.redo();
            }
            setActive(isActive);
        } catch (@NotNull final IndexOutOfBoundsException ignored) {
        }
    }

    /**
     * Returns whether an undo can be performed.
     *
     * @return true if undo can be performed.
     */
    public boolean canUndo() {
        return history.previous() > 0;
    }

    /**
     * Returns whether a redo can be performed.
     *
     * @return true if redo can be performed.
     */
    public boolean canRedo() {
        return history.upcoming() > 0;
    }

    /**
     * Reset the History.
     *
     * @param initial the initial text.
     */
    public void reset(final String initial) {
        history.reset(new FileHistoryObject(textComponent, 0, initial, "", ChangeType.INSERT));
    }

    /**
     * Set the history capacity.
     *
     * @param capacity history capacity
     */
    public void setCapacity(final int capacity) {
        history.setCapacity(capacity);
    }

    /**
     * Returns whether the history controller is currently active.
     *
     * @return true if active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Set whether the Controller should respond to changes in the document.
     *
     * @param active true if it should respond
     */
    public void setActive(final boolean active) {
        this.active = active;
    }

    /**
     * Get the history.
     *
     * @return the history
     */
    @NotNull
    public History<FileHistoryObject> getHistory() {
        return history;
    }
}
