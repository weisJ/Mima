package edu.kit.mima.gui.components.dialog;

import com.intellij.ui.DocumentAdapter;
import edu.kit.mima.gui.components.alignment.Alignment;
import edu.kit.mima.gui.components.tooltip.DefaultTooltipWindow;
import edu.kit.mima.gui.icons.Icons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.plaf.basic.BasicOptionPaneUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * @author Jannis Weis
 * @since 2019
 */
public final class FileDialog extends JOptionPane {

    private static final FileDialog dialog = new FileDialog();
    private final DefaultTooltipWindow errorTooltip;
    private final DefaultTooltipWindow.TooltipPanel tooltipPanel;
    private final JComponent glassPane;
    private File directory;
    private String extension;
    private JTextField textField;
    private JButton okButton;
    private JButton cancelButton;
    private boolean error = false;
    private boolean wantDirectory;


    private FileDialog() {
        super("Enter new file name:", JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        setWantsInput(true);
        textField.setColumns(30);

        errorTooltip = new DefaultTooltipWindow("");
        errorTooltip.setAlignment(Alignment.CENTER);
        errorTooltip.setTooltipFont(errorTooltip.getTooltipFont().deriveFont(11.0f));
        errorTooltip.setRoundCorners(false);
        errorTooltip.setTooltipBackground(UIManager.getColor("FileDialog.error"));
        errorTooltip.setTooltipBorderColor(UIManager.getColor("FileDialog.errorBorder"));

        tooltipPanel = errorTooltip.getTooltipPanel();

        glassPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        glassPane.setOpaque(false);
        glassPane.add(tooltipPanel);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                tooltipPanel.hideAndShow(1000, FileDialog.this);
            }
        });


        textField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull final DocumentEvent documentEvent) {
                var text = textField.getText();
                if (hasError(text)) {
                    if (!error || !tooltipPanel.isVisible()) {
                        okButton.setEnabled(false);
                        textField.putClientProperty("error", Boolean.TRUE);
                        errorTooltip.setText(getErrorMessage(text));
                        glassPane.setVisible(true);
                        tooltipPanel.showTooltip(FileDialog.this);
                        repaint();
                    }
                    error = true;
                } else {
                    if (error || tooltipPanel.isVisible()) {
                        tooltipPanel.hideTooltip(FileDialog.this);
                        okButton.setEnabled(true);
                        textField.putClientProperty("error", Boolean.FALSE);
                        repaint();
                    }
                    error = false;
                }
            }
        });
    }

    public static File showFileDialog(final Component parentComponent, final File folder, final String extension) {
        return showDialog(parentComponent, folder, extension, "New File", false);
    }

    public static File showFolderDialog(final Component parentComponent, final File folder) {
        return showDialog(parentComponent, folder, null, "New Folder", true);

    }

    private static File showDialog(final Component parentComponent, final File folder,
                                   final String extension, final String title, final boolean wantDirectory) {
        String titleMessage = title;
        if (extension != null && !extension.isEmpty()) {
            titleMessage += " (." + extension + ')';
        }
        if (wantDirectory) {
            dialog.setMessage(new JLabel("Enter new folder name:", Icons.FOLDER, SwingConstants.LEFT));
        } else {
            Icon icon = extension == null || extension.isBlank() ? Icons.GENERAL_FILE
                                                                 : Icons.forFile("." + extension);
            dialog.setMessage(new JLabel("Enter new file name:", icon, SwingConstants.LEFT));
        }

        dialog.directory = folder.isDirectory() ? folder : folder.getParentFile();
        dialog.extension = extension;
        dialog.wantDirectory = wantDirectory;
        dialog.prepare();

        JDialog window = dialog.createDialog(parentComponent, titleMessage);
        window.setGlassPane(dialog.glassPane);

        dialog.selectInitialValue();
        window.setVisible(true);
        window.dispose();

        Object value = dialog.getInputValue();

        if (value == UNINITIALIZED_VALUE || value == null) {
            return null;
        }
        return folder.toPath().resolve(ensureExtension((String) value, extension)).toFile();
    }

    private static String ensureExtension(String name, final String extension) {
        if (extension == null) {
            return name;
        }
        if (!extension.isEmpty()) {
            if (!name.endsWith(extension)) {
                name += "." + extension;
            }
        }
        return name;
    }

    private boolean hasError(final String text) {
        return (wantDirectory && text.contains("."))
               || !text.isEmpty() && fileExists(directory, ensureExtension(text, extension));
    }

    private String getErrorMessage(final String text) {
        if (!wantDirectory) {
            return "A file with name '" + text + "' already exists.";
        } else {
            if (text.contains(".")) {
                return "Not a valid folder name.";
            } else {
                return "A folder with name '" + text + "' already exists.";
            }
        }
    }

    private boolean fileExists(@NotNull final File folder, @NotNull final String name) {
        return folder.toPath().resolve(name.trim()).toFile().exists();
    }


    private void prepare() {
        textField.setText("");
        okButton.setEnabled(true);
        cancelButton.setEnabled(true);
    }

    @Override
    public void updateUI() {
        setUI(new FileDialogOptionPaneUI());
    }

    private class FileDialogOptionPaneUI extends BasicOptionPaneUI {

        @Override
        protected Container createMessageArea() {
            var container = super.createMessageArea();
            textField = (JTextField) inputComponent;
            return container;
        }

        @Override
        protected void addButtonComponents(final Container container, final Object[] buttons, final int initialIndex) {
            super.addButtonComponents(container, buttons, initialIndex);
            okButton = (JButton) container.getComponent(0);
            cancelButton = (JButton) container.getComponent(1);
        }

        @Override
        protected Object getMessage() {
            var obj = super.getMessage();
            if (obj instanceof Object[]) {
                var msg = (Object[]) obj;
                return new Object[]{msg[0], createSeparator(), msg[1]};
            }
            return obj;
        }

        @Override
        protected Container createSeparator() {
            return new JPanel() {
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(0, 5);
                }
            };
        }
    }
}
