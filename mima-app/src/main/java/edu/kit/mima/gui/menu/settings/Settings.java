package edu.kit.mima.gui.menu.settings;

import edu.kit.mima.gui.components.border.AdaptiveLineBorder;
import edu.kit.mima.gui.components.fontchooser.FontChooser;
import edu.kit.mima.gui.components.fontchooser.model.FontSelectionModel;
import edu.kit.mima.gui.components.fontchooser.panes.AbstractPreviewPane;
import edu.kit.mima.gui.menu.CardPanelBuilder;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import org.jetbrains.annotations.NotNull;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Setting Dialog for Mima App.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class Settings extends JDialog {

    private static final Dimension SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    private static Settings instance;
    private Component parent;
    private Preferences backup;

    private Settings() {
        backup = Preferences.getInstance().clone();
        setIconImage(Toolkit.getDefaultToolkit().getImage(
                getClass().getClassLoader().getResource("images/mima.png")));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                hideWindow();
            }
        });
        setSize((int) SIZE.getWidth() / 3, (int) SIZE.getHeight() / 3);
        setTitle("Settings");
        setLocationRelativeTo(null);
        setResizable(true);
        initializeComponents();
    }

    /**
     * Get the settings instance.
     *
     * @return Settings instance.
     */
    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    /**
     * Show the settings dialog.
     *
     * @param parent parent component.
     */
    public static void showWindow(@NotNull final Component parent) {
        final Settings s = getInstance();
        s.setLocationRelativeTo(parent);
        s.setVisible(true);
        parent.setEnabled(false);
        s.backup = Preferences.getInstance().clone();
        s.parent = parent;
    }

    /**
     * Hide the settings dialog.
     */
    public static void hideWindow() {
        var s = getInstance();
        s.setVisible(false);
        s.parent.setFocusable(true);
        s.parent.setEnabled(true);
        s.parent.requestFocus();
        ((JFrame) s.parent).toFront();
        s.parent.repaint();
    }

    /**
     * Returns whether the settings dialog is open.
     *
     * @return true if open
     */
    public static boolean isOpen() {
        return instance != null && instance.isVisible();
    }

    /**
     * Close the Help Window.
     */
    public static void close() {
        if (instance != null) {
            instance.dispose();
        }
    }


    private void initializeComponents() {
        setLayout(new BorderLayout());
        var buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        var cancel = new JButton();
        var apply = new JButton();
        var ok = new JButton();
        ok.setDefaultCapable(true);
        cancel.setAction(new AbstractAction("Cancel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                hideWindow();
            }
        });
        apply.setAction(new AbstractAction("Apply") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Preferences.getInstance().save(backup);
            }
        });
        ok.setAction(new AbstractAction("Ok") {
            @Override
            public void actionPerformed(ActionEvent e) {
                apply.doClick();
                hideWindow();
            }
        });
        buttonPanel.setBorder(new AdaptiveLineBorder(1, 0, 0, 0, "Border.line1"));
        buttonPanel.add(ok);
        buttonPanel.add(cancel);
        buttonPanel.add(apply);

        var content = new CardPanelBuilder().addItem("General").addSetting("Theme",
                                                                           createThemeChooser())
                                            .addItem("Editor",
                         createFontChooserPanel(PropertyKey.EDITOR_FONT, new EditorPreview()))
                                            .addItem("Console",
                         createFontChooserPanel(PropertyKey.CONSOLE_FONT, new ConsolePreview())).addItem(
                        "View").addSetting("Show Binary:", new JCheckBox()).create(this);
        add(content, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        ok.getRootPane().setDefaultButton(ok);
    }

    private JComponent createThemeChooser() {
        var combo = new JComboBox<>(new String[]{"Light", "Dark"});
        combo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                backup.saveString(PropertyKey.THEME, e.getItem().toString());
            }
        });
        combo.setSelectedItem(Preferences.getInstance().readString(PropertyKey.THEME));
        return combo;
    }

    @NotNull
    private JComponent createFontChooserPanel(@NotNull final PropertyKey key,
                                              @NotNull final AbstractPreviewPane previewPane) {
        final FontChooser fontChooser
                = new FontChooser(Preferences.getInstance().readFont(key), previewPane);
        fontChooser.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        fontChooser.addChangeListener(event -> {
            final FontSelectionModel model = (FontSelectionModel) event.getSource();
            backup.saveFont(key, model.getSelectedFont());
        });
        return fontChooser;
    }
}
