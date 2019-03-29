package edu.kit.mima.gui.components.editor;

import edu.kit.mima.gui.components.BreakpointComponent;
import edu.kit.mima.gui.components.NumberedTextPane;
import edu.kit.mima.gui.components.editor.highlighter.Highlighter;
import edu.kit.mima.gui.components.editor.view.HighlightViewFactory;
import edu.kit.mima.preferences.ColorKey;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import edu.kit.mima.preferences.UserPreferenceChangedListener;
import edu.kit.mima.util.HSLColor;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.ViewFactory;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusListener;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Editor that supports highlighting and text history. If the instance of an Editor isn't used
 * anymore the {@link #close} method should be called to prevent the history from remaining in
 * memory.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class Editor extends JScrollPane implements UserPreferenceChangedListener, AutoCloseable {

    @NotNull private final NumberedTextPane numberedTextPane;
    @NotNull private final TextHistoryController historyController;
    @NotNull private final List<EditEventHandler> editEventHandlers;

    private Highlighter highlighter;
    private boolean changeLock;
    private boolean repaint;
    private int currentMark = -1;

    /**
     * Editor that supports highlighting and text history.
     */
    public Editor() {
        Preferences.registerUserPreferenceChangedListener(this);
        final var pref = Preferences.getInstance();
        final Font font = pref.readFont(PropertyKey.EDITOR_FONT);
        final Color breakpointColor = new HSLColor(getBackground())
                .adjustHue(5).adjustShade(30).adjustSaturation(30).getRGB();

        numberedTextPane = new NumberedTextPane(this,
                                                new Font("Monospaced", Font.PLAIN, 12),
                                                pref.readColor(ColorKey.EDITOR_TEXT_SECONDARY));
        final var editorPane = numberedTextPane.getPane();
        numberedTextPane.addIndexListener(index -> {
            if (numberedTextPane.hasComponentAt(index)) {
                editorPane.unmarkLine(index);
                numberedTextPane.removeComponentAt(index);
            } else {
                editorPane.markLine(index, breakpointColor);
                numberedTextPane.addComponentAt(new BreakpointComponent(index), index);
            }
        });

        final JPanel textPanel = new JPanel();
        editorPane.setFont(font);
        editorPane.setEditorKit(new StyledEditorKit() {
            @NotNull
            public ViewFactory getViewFactory() {
                return new HighlightViewFactory();
            }
        });

        textPanel.setLayout(new BorderLayout());
        textPanel.add(numberedTextPane, BorderLayout.LINE_START);
        textPanel.add(editorPane, BorderLayout.CENTER);
        setViewportView(textPanel);
        getVerticalScrollBar().setUnitIncrement(font.getSize() / 2);

        historyController = new TextHistoryController(editorPane, 0);
        historyController.setActive(false);
        editEventHandlers = new ArrayList<>();
        repaint = true;

        final StyledDocument document = editorPane.getStyledDocument();
        ((AbstractDocument) document)
                .setDocumentFilter(new EditorDocumentFilter(this, historyController));
        editorPane.setBackground(pref.readColor(ColorKey.EDITOR_BACKGROUND));
        editorPane.setCaretColor(pref.readColor(ColorKey.EDITOR_TEXT));
    }

    /**
     * Call steps performed after an edit has occurred.
     */
    public void notifyEdit() {
        if (changeLock) {
            return;
        }
        for (final EditEventHandler event : editEventHandlers) {
            event.notifyEdit();
        }
        update();
    }

    /**
     * Get all placed breakpoints.
     *
     * @return Array of breakpoints.
     */
    @NotNull
    public BreakpointComponent[] getBreakpoints() {
        return Arrays.stream(numberedTextPane.getIndexComponents())
                .map(c -> (BreakpointComponent) c).toArray(BreakpointComponent[]::new);
    }

    /**
     * Update the document.
     */
    private void update() {
        if (!repaint) {
            return;
        }
        changeLock = true;
        final boolean historyLock = historyController.isActive();
        historyController.setActive(false);
        final int caret = numberedTextPane.getPane().getCaretPosition();
        if (highlighter != null) {
            highlighter.updateHighlighting(numberedTextPane.getPane());
        }
        setCaretPosition(caret);
        changeLock = false;
        historyController.setActive(historyLock);
    }

    /**
     * Undo last file change.
     */
    public void undo() {
        historyController.undo();
        update();
    }

    /**
     * Redo the last undo.
     */
    public void redo() {
        historyController.redo();
        update();
    }

    /**
     * Returns whether an undo can be performed.
     *
     * @return true if undo can be performed.
     */
    public boolean canUndo() {
        return historyController.canUndo();
    }

    /**
     * Returns whether a redo can be performed.
     *
     * @return true if redo can be performed.
     */
    public boolean canRedo() {
        return historyController.canRedo();
    }

    /**
     * Transform current line in editor.
     *
     * @param function Function that takes in the current line and caret position in line
     * @param index    index in file
     */
    public void transformLine(@NotNull final Function<String, String> function, final int index) {
        final var editorPane = numberedTextPane.getPane();
        final String text = editorPane.getText();
        final int lower = text.substring(0, index).lastIndexOf('\n') + 1;
        final int upper = text.substring(index).indexOf('\n') + index;
        final String newLine = function.apply(text.substring(lower, upper));
        try {
            editorPane.getDocument().remove(lower, upper - lower);
            editorPane.getDocument().insertString(lower, newLine, new SimpleAttributeSet());
        } catch (@NotNull final BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Insert String into text.
     *
     * @param text   text to insert
     * @param offset location in file
     * @throws BadLocationException if location is outside bounds
     */
    public void insert(final String text, final int offset) throws BadLocationException {
        numberedTextPane.getPane()
                .getStyledDocument().insertString(offset, text, new SimpleAttributeSet());
    }

    /**
     * Select line in editor.
     *
     * @param index line to select
     */
    public void selectLine(final int index) {
        numberedTextPane.getPane().selectLine(index);
    }

    /**
     * Mark a line. At most one line can me marked at any time.
     *
     * @param index index of line
     */
    public void markLine(final int index) {
        numberedTextPane.getPane().unmarkLine(currentMark);
        numberedTextPane.getPane().markLine(index, new Color(0x2D71D2)); //Todo decouple colour
        currentMark = index;
    }

    /*----------Getter-and-Setter----------*/


    /**
     * Get the current font.
     *
     * @return current font
     */
    public Font getEditorFont() {
        return numberedTextPane.getPane().getFont();
    }

    /**
     * Set the current font.
     *
     * @param font font to use
     */
    public void setEditorFont(@NotNull final Font font) {
        numberedTextPane.getPane().setFont(font);
        getVerticalScrollBar().setUnitIncrement(font.getSize() / 2);
        repaint();
    }

    /**
     * Set Highlighter to use for syntax highlighting.
     *
     * @param highlighter highlighter
     */
    public void setHighlighter(final Highlighter highlighter) {
        this.highlighter = highlighter;
    }

    /**
     * Add an action that should be performed after an edit to the text has been occurred.
     *
     * @param handler handler to add
     */
    public void addEditEventHandler(final EditEventHandler handler) {
        editEventHandlers.add(handler);
    }


    @Override
    public synchronized void addMouseListener(final MouseListener l) {
        super.addMouseListener(l);
        numberedTextPane.getPane().addMouseListener(l);
    }

    @Override
    public synchronized void removeMouseListener(final MouseListener l) {
        super.removeMouseListener(l);
        numberedTextPane.getPane().removeMouseListener(l);
    }

    /**
     * Remove EditEventHandler.
     *
     * @param handler handler to remove
     * @return true if removed successfully
     */
    public boolean removeEditEventHandler(final EditEventHandler handler) {
        return editEventHandlers.remove(handler);
    }

    /**
     * Get the text contained in the editor.
     *
     * @return text in editor
     */
    public String getText() {
        return numberedTextPane.getPane().getText();
    }

    /**
     * Set the text in the editor.
     *
     * @param text text to set
     */
    public void setText(@NotNull final String text) {
        final int pos = getCaretPosition();
        try {
            historyController.addReplaceHistory(0,
                                                numberedTextPane.getPane().getText().length(),
                                                text);
        } catch (@NotNull final BadLocationException ignored) {
        }
        historyController.setActive(false);
        numberedTextPane.getPane().setText(text);
        historyController.setActive(true);
        setCaretPosition(Math.max(Math.min(pos, text.length() - 1), 0));
        update();
    }

    @Override
    public synchronized void addFocusListener(FocusListener l) {
        numberedTextPane.getPane().addFocusListener(l);
    }

    @Override
    public synchronized void removeFocusListener(FocusListener l) {
        numberedTextPane.getPane().removeFocusListener(l);
    }

    /**
     * Get the current caret position.
     *
     * @return caret position
     */
    public int getCaretPosition() {
        return numberedTextPane.getPane().getCaretPosition();
    }

    /**
     * Set the caret position.
     *
     * @param caretPosition new caret Position
     */
    public void setCaretPosition(final int caretPosition) {
        numberedTextPane.getPane().setCaretPosition(caretPosition);
    }

    /**
     * Set the limit of characters per line. The Editor will then be drawing a line at this
     * position. A value <= 0 signals that no line should be drawn.
     *
     * @param limit character limit
     */
    public void showCharacterLimit(final int limit) {
        numberedTextPane.getPane().setVertLine(limit);
    }

    /**
     * Use change history. If yes undo/redo will be supported.
     *
     * @param useHistory whether to use history
     * @param capacity   capacity of history
     */
    public void useHistory(final boolean useHistory, final int capacity) {
        historyController.reset();
        historyController.setActive(useHistory);
        historyController.setCapacity(capacity);
    }

    /**
     * Set whether the editor should be accented or not.
     *
     * @param repaint true if accented.
     */
    public void setRepaint(final boolean repaint) {
        this.repaint = repaint;
        numberedTextPane.getPane().setIgnoreRepaint(!repaint);
    }

    @Override
    public void notifyUserPreferenceChanged(@NotNull final PropertyKey key) {
        final var pref = Preferences.getInstance();
        if (key == PropertyKey.THEME) {
            final var editorPane = numberedTextPane.getPane();
            editorPane.setBackground(pref.readColor(ColorKey.EDITOR_BACKGROUND));
            editorPane.setCaretColor(pref.readColor(ColorKey.EDITOR_TEXT));
            numberedTextPane.setNumberingColor(pref.readColor(ColorKey.EDITOR_TEXT_SECONDARY));
        } else if (key == PropertyKey.EDITOR_FONT) {
            setEditorFont(pref.readFont(PropertyKey.EDITOR_FONT));
        } else if (key == PropertyKey.EDITOR_HISTORY) {
            historyController.setActive(pref.readBoolean(key));
        } else if (key == PropertyKey.EDITOR_HISTORY_SIZE) {
            historyController.setCapacity(pref.readInteger(key));
        }
    }

    @Override
    public void close() {
        historyController.getHistory().close();
    }

    /**
     * Get preview Image of editor content.
     *
     * @return preview image.
     */
    public BufferedImage createPreviewImage() {
        var b = numberedTextPane.getPane().getBounds();
        BufferedImage image = new BufferedImage(b.width / 2,
                                                Math.min(b.height / 4, b.width),
                                                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                           RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        numberedTextPane.getPane().paint(g);
        BufferedImage scaledImage = new BufferedImage(image.getWidth() / 2,
                                                      image.getHeight() / 2,
                                                      BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = scaledImage.createGraphics();
        AffineTransform xform = AffineTransform.getScaleInstance(0.5, 0.5);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics2D.drawImage(image, xform, null);
        graphics2D.dispose();
        return scaledImage;
    }
}
