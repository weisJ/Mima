package edu.kit.mima.gui.components.dialog;

import com.intellij.ui.DocumentAdapter;
import com.weis.darklaf.icons.EmptyIcon;
import edu.kit.mima.gui.icon.Icons;
import edu.kit.mima.api.util.FileName;
import com.weis.darklaf.components.alignment.Alignment;
import com.weis.darklaf.components.TextFieldHistory;
import edu.kit.mima.gui.components.tooltip.DefaultTooltipWindow;
import edu.kit.mima.util.Format;
import edu.kit.mima.util.IconUtil;
import org.jetbrains.annotations.Contract;
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
    private static final TextFieldHistory directoryHistory = new TextFieldHistory(null, 20, 100);
    private final DefaultTooltipWindow errorTooltip;
    private final DefaultTooltipWindow.TooltipPanel tooltipPanel;
    private final JComponent glassPane;
    private File directory;
    private String extension;
    private JTextField textField;
    private JLabel inputDescriptor;
    private JButton okButton;
    private JButton cancelButton;
    private boolean error = false;
    private boolean wantDirectory;
    private boolean isDestinationChooser;


    private FileDialog() {
        super("Enter new file name:", JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        setWantsInput(true);
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

    public static File showCopyFileDialog(final Component parentComponent, final File folder, final File initial) {
        return showDialog(parentComponent, folder, null, "Copy File", initial.getName(),
                          String.format(Format.BOLD, "Copy file " + initial.getAbsolutePath()),
                          "New Name", false, false, false);
    }

    public static File showCopyFilesDialog(final Component parentComponent, final File folder) {
        return showDialog(parentComponent, folder, null, "Copy File", folder.getAbsolutePath(),
                          String.format(Format.BOLD, "Copy files to " + folder.getAbsolutePath()),
                          "Copy to", false, false, true);
    }

    public static File showFileDialog(final Component parentComponent, final File folder, final String extension) {
        return showDialog(parentComponent, folder, extension, "New File", "",
                          "Enter new file name:", "Name", false,
                          true, false);
    }

    public static File showFolderDialog(final Component parentComponent, final File folder) {
        return showDialog(parentComponent, folder, null, "New Folder", "",
                          "Enter new folder name:", "Name", true,
                          true, false);

    }

    private static File showDialog(final Component parentComponent, final File folder,
                                   final String extension, final String title, final String initialValue,
                                   final String message, final String inputLabel,
                                   final boolean wantDirectory,
                                   final boolean showIcon,
                                   final boolean destination) {
        String titleMessage = title;
        if (extension != null && !extension.isEmpty()) {
            titleMessage += " (." + extension + ')';
        }
        Icon icon = !showIcon
                    ? EmptyIcon.create(0)
                    : wantDirectory
                      ? Icons.FOLDER
                      : extension == null || extension.isBlank()
                        ? Icons.GENERAL_FILE
                        : IconUtil.forFile("." + extension);
        var panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(new JLabel(message, icon, SwingConstants.LEFT));
        dialog.setMessage(panel);
        dialog.directory = folder.isDirectory() ? folder : folder.getParentFile();
        dialog.extension = extension;
        dialog.wantDirectory = wantDirectory;
        dialog.isDestinationChooser = destination;
        dialog.prepare(initialValue, inputLabel);

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

    @Contract("_, null -> param1")
    private static String ensureExtension(String name, final String extension) {
        if (extension == null) {
            return name;
        }
        if (!extension.isEmpty()) {
            if (!FileName.isExtension(name, extension)) {
                name += "." + extension;
            }
        }
        return name;
    }

    private boolean hasError(final String text) {
        if (!isDestinationChooser && FileName.isValidFileName(text)) {
            if (!wantDirectory) {
                return !text.isEmpty() && fileExists(directory, ensureExtension(text, extension));
            }
            return true;
        }
        return false;
    }

    private String getErrorMessage(final String text) {
        if (!wantDirectory) {
            return "A file with name '" + text + "' already exists.";
        } else if (isDestinationChooser) {
            return "Directory does not exist.";
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


    private void prepare(final String initial, final String inputLabel) {
        textField.setText(initial);
        textField.setColumns(0);
        inputDescriptor.setText(inputLabel);
        okButton.setEnabled(true);
        cancelButton.setEnabled(true);
        if (isDestinationChooser) {
            textField.putClientProperty("JTextField.Search.FindPopup", directoryHistory);
        } else {
            textField.putClientProperty("JTextField.Search.FindPopup", null);
        }
    }

    @Override
    public void updateUI() {
        setUI(new FileDialogOptionPaneUI());
    }

    private class FileDialogOptionPaneUI extends BasicOptionPaneUI {

        @Override
        protected Container createMessageArea() {
            JPanel cont = (JPanel) super.createMessageArea();
            textField = (JTextField) inputComponent;
            return cont;
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
            var panel = new JPanel();
            if (inputDescriptor == null) {
                inputDescriptor = new JLabel();
            }
            panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
            panel.add(inputDescriptor);
            if (obj instanceof Object[]) {
                var msg = (Object[]) obj;
                panel.add(createSeparator());
                panel.add((Component) msg[1]);
                return new Object[]{msg[0], createSeparator(), panel};
            }
            return new Object[]{obj, createSeparator(), panel};
        }

        @Override
        protected Container createSeparator() {
            return new JPanel() {
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(5, 5);
                }
            };
        }

        protected Container createButtonArea() {
            var bottom = super.createButtonArea();
            bottom.setLayout(new FlowLayout(FlowLayout.RIGHT));
            return bottom;
        }
    }
}
