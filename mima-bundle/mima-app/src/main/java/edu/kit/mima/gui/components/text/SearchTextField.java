/* Copyright 2000-2017 JetBrains s.r.o.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package edu.kit.mima.gui.components.text;

import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyListener;

/**
 * A TextField with search field appearance.
 */
public class SearchTextField extends JPanel {
    @NotNull
    private final JTextField textField;

    /**
     * Create new TextField that represents a search field.
     */
    public SearchTextField() {
        super(new BorderLayout());
        textField = new JTextField();
        textField.setColumns(15);
        add(textField, BorderLayout.CENTER);

        textField.putClientProperty("JTextField.variant", "search");
    }

    /**
     * Add a document listener.
     *
     * @param listener the listener to add.
     */
    public void addDocumentListener(final DocumentListener listener) {
        getTextEditor().getDocument().addDocumentListener(listener);
    }

    /**
     * Remove a document listener.
     *
     * @param listener the listener to remove.
     */
    public void removeDocumentListener(final DocumentListener listener) {
        getTextEditor().getDocument().removeDocumentListener(listener);
    }

    /**
     * Add capability for a history popup.
     *
     * @param menu the popup to use.
     */
    public void addHistoryPopup(final JPopupMenu menu) {
        textField.putClientProperty("JTextField.Search.FindPopup", menu);
    }

    /**
     * Add a keyboard listener.
     *
     * @param listener the listener to add.
     */
    public void addKeyboardListener(final KeyListener listener) {
        getTextEditor().addKeyListener(listener);
    }

    /**
     * Get the text.
     *
     * @return the text.
     */
    public String getText() {
        return getTextEditor().getText();
    }

    /**
     * Set the text.
     *
     * @param text the text.
     */
    public void setText(final String text) {
        getTextEditor().setText(text);
    }

    /**
     * Select the text.
     */
    public void selectText() {
        getTextEditor().selectAll();
    }

    /**
     * Get the text field.
     *
     * @return the text field.
     */
    @NotNull
    public JTextField getTextEditor() {
        return textField;
    }

    @Override
    public boolean requestFocusInWindow() {
        return textField.requestFocusInWindow();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        Border border = super.getBorder();
        if (border != null && UIUtil.isUnderAquaLookAndFeel()) {
            var insets = border.getBorderInsets(this);
            size.width += insets.left + insets.right;
            size.height += insets.top + insets.bottom;
        }
        return size;
    }
}
