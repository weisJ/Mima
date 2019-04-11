package edu.kit.mima.gui.menu.settings;

import edu.kit.mima.gui.components.LookAndFeelInfoCellRenderer;
import edu.kit.mima.gui.components.fontchooser.FontChooser;
import edu.kit.mima.gui.components.fontchooser.model.FontSelectionModel;
import edu.kit.mima.gui.components.fontchooser.panes.AbstractPreviewPane;
import edu.kit.mima.gui.laf.DarkLafInfo;
import edu.kit.mima.gui.laf.LafManager;
import edu.kit.mima.gui.laf.LightLafInfo;
import edu.kit.mima.gui.menu.CardPanelBuilder;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private Settings() {
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
        new CardPanelBuilder()
                .addItem("General")
                .addItem("Theme")
                .addSetting("Editor:", new JComboBox<>(new String[]{"Light", "Dark"}))
                .addItem("Editor",
                         createFontChooserPanel(PropertyKey.EDITOR_FONT, new EditorPreview()))
                .addItem("Console",
                         createFontChooserPanel(PropertyKey.CONSOLE_FONT, new ConsolePreview()))
                .addItem("View")
                .addSetting("Show Binary:", new JCheckBox())
                .addToComponent(this);
    }

    @NotNull
    private JComponent createFontChooserPanel(@NotNull final PropertyKey key,
                                              @NotNull final AbstractPreviewPane previewPane) {
        final FontChooser fontChooser
                = new FontChooser(Preferences.getInstance().readFont(key), previewPane);
        fontChooser.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        fontChooser.addChangeListener(event -> {
            final FontSelectionModel model = (FontSelectionModel) event.getSource();
            Preferences.getInstance().saveFont(key, model.getSelectedFont());
        });
        return fontChooser;
    }

    @NotNull
    private JComponent createThemeChooser() {
        final UIManager.LookAndFeelInfo[] plaf = UIManager.getInstalledLookAndFeels();
        final List<UIManager.LookAndFeelInfo> loafs = new ArrayList<>();
        loafs.add(new LightLafInfo());
        loafs.add(new DarkLafInfo());
        loafs.addAll(Arrays.asList(plaf));
        final JComboBox<UIManager.LookAndFeelInfo> comboBox =
                new JComboBox<>(loafs.toArray(UIManager.LookAndFeelInfo[]::new));
        comboBox.setEditable(false);
        comboBox.setRenderer(new LookAndFeelInfoCellRenderer());
        comboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                final UIManager.LookAndFeelInfo info = (UIManager.LookAndFeelInfo) e.getItem();
                final var pref = Preferences.getInstance();
                pref.saveString(PropertyKey.THEME, info.getName());
                pref.saveString(PropertyKey.THEME_PATH, info.getClassName());
            }
        });
        int index = 0;
        for (final UIManager.LookAndFeelInfo info : loafs) {
            if (info.getName().equals(LafManager.getCurrentLaf())) {
                break;
            }
            index++;
        }
        comboBox.setSelectedIndex(index);
        return comboBox;
    }

    @Nullable
    @Contract(pure = true)
    private JComponent createSyntaxChooser() {
        return null; //Todo
    }
}
