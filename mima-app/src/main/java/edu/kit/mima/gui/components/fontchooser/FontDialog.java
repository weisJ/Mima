/*
 * A font chooser JavaBean component.
 * Copyright (C) 2009 Dr Christos Bohoris
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3 as published by the Free Software Foundation;
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * swing@connectina.com
 */

package edu.kit.mima.gui.components.fontchooser;

import edu.kit.mima.gui.components.fontchooser.util.ResourceBundleUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

/**
 * A dialog containing a {@code FontChooser} as well as OK and Cancel buttons.
 *
 * @author Christos Bohoris
 */
public class FontDialog extends JDialog {

    private final FontChooser chooser = new FontChooser();
    private final JButton cancelButton = new JButton();
    private final JButton okButton = new JButton();
    private final ResourceBundle bundle = ResourceBundle.getBundle("FontDialog");
    private final ResourceBundleUtil resourceBundleUtil = new ResourceBundleUtil(bundle);
    private boolean cancelSelected;

    public FontDialog() {
        initDialog();
    }

    public FontDialog(final Frame owner) {
        super(owner);
        initDialog();
    }

    public FontDialog(final Frame owner, final boolean modal) {
        super(owner, modal);
        initDialog();
    }

    public FontDialog(final Frame owner, final String title) {
        super(owner, title);
        initDialog();
    }

    public FontDialog(final Frame owner, final String title, final boolean modal) {
        super(owner, title, modal);
        initDialog();
    }

    public FontDialog(final Frame owner, final String title,
                      final boolean modal, final GraphicsConfiguration gc) {
        super(owner, title, modal, gc);
        initDialog();
    }

    public FontDialog(final Dialog owner) {
        super(owner);
        initDialog();
    }

    public FontDialog(final Dialog owner, final boolean modal) {
        super(owner, modal);
        initDialog();
    }

    public FontDialog(final Dialog owner, final String title) {
        super(owner, title);
        initDialog();
    }

    public FontDialog(final Dialog owner, final String title, final boolean modal) {
        super(owner, title, modal);
        initDialog();
    }

    public FontDialog(final Dialog owner, final String title,
                      final boolean modal, final GraphicsConfiguration gc) {
        super(owner, title, modal, gc);
        initDialog();
    }

    public FontDialog(final Window owner) {
        super(owner);
        initDialog();
    }

    public FontDialog(final Window owner, final ModalityType modalityType) {
        super(owner, modalityType);
        initDialog();
    }

    public FontDialog(final Window owner, final String title) {
        super(owner, title);
        initDialog();
    }

    public FontDialog(final Window owner, final String title, final ModalityType modalityType) {
        super(owner, title, modalityType);
        initDialog();
    }

    public FontDialog(final Window owner, final String title,
                      final ModalityType modalityType, final GraphicsConfiguration gc) {
        super(owner, title, modalityType, gc);
        initDialog();
    }

    /**
     * Show Font Chooser Dialog.
     *
     * @param component parent component.
     */
    public static void showDialog(@NotNull final Component component) {
        final FontDialog dialog = new FontDialog((Frame) null, "Select Font", true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setSelectedFont(component.getFont());
        dialog.setVisible(true);
        if (!dialog.isCancelSelected()) {
            component.setFont(dialog.getSelectedFont());
        }
    }

    private void initDialog() {
        initComponents();
        getRootPane().setDefaultButton(okButton);

        cancelButton.addActionListener(event -> cancelSelected = true);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                cancelSelected = true;
            }
        });
    }

    private void initComponents() {

        final JPanel chooserPanel = new JPanel();
        chooserPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 11));
        chooserPanel.setLayout(new BorderLayout(0, 12));
        chooserPanel.add(chooser);
        add(chooserPanel);

        final JPanel controlPanel = new JPanel();
        controlPanel.setBorder(BorderFactory.createEmptyBorder(7, 7, 6, 6));
        controlPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
        add(controlPanel, BorderLayout.PAGE_END);

        okButton.setMnemonic(resourceBundleUtil.getFirstChar("action.ok.mnemonic"));
        okButton.setText(bundle.getString("action.ok"));
        okButton.addActionListener(event -> dispose());
        controlPanel.add(okButton);

        cancelButton.setMnemonic(resourceBundleUtil.getFirstChar("action.cancel.mnemonic"));
        cancelButton.setText(bundle.getString("action.cancel"));
        cancelButton.addActionListener(event -> {
            cancelSelected = true;
            dispose();
        });
        controlPanel.add(cancelButton);

        pack();
    }

    @Nullable
    public Font getSelectedFont() {
        return chooser.getSelectedFont();
    }

    private void setSelectedFont(@NotNull final Font font) {
        chooser.setSelectedFont(font);
    }

    @Contract(pure = true)
    private boolean isCancelSelected() {
        return cancelSelected;
    }
}
