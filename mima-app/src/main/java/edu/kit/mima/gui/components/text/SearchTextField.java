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

import com.bulenkov.iconloader.util.JBInsets;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;

/**
 * A TextField with search field appearance.
 */
public class SearchTextField extends JPanel {
    @NotNull
    private final JTextField myTextField;

    /**
     * Create new TextField that represents a search field.
     */
    public SearchTextField() {
        super(new BorderLayout());
        myTextField = new JTextField();
        myTextField.setColumns(15);
        add(myTextField, BorderLayout.CENTER);

        myTextField.putClientProperty("JTextField.variant", "search");
        myTextField.putClientProperty("JTextField.Search.CancelAction",
                                      (ActionListener) e -> myTextField.setText(""));
    }

    /**
     * Add a document listener.
     *
     * @param listener the listener to add.
     */
    public void addDocumentListener(DocumentListener listener) {
        getTextEditor().getDocument().addDocumentListener(listener);
    }

    /**
     * Remove a document listener.
     *
     * @param listener the listener to remove.
     */
    public void removeDocumentListener(DocumentListener listener) {
        getTextEditor().getDocument().removeDocumentListener(listener);
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
    public void setText(String text) {
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
        return myTextField;
    }

    @Override
    public boolean requestFocusInWindow() {
        return myTextField.requestFocusInWindow();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        Border border = super.getBorder();
        if (border != null && UIUtil.isUnderAquaLookAndFeel()) {
            JBInsets.addTo(size, border.getBorderInsets(this));
        }
        return size;
    }
}