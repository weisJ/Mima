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

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.BorderLayout;
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
 * @author Jannis Weis
 * @since 2018
 */
public final class Settings extends JDialog {

    private static final Dimension SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    private static Settings instance;
    private static Component parent;

    private Settings() {
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("images/mima.png")));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                hideWindow();
            }
        });
        setSize((int) SIZE.getWidth() / 3, (int) SIZE.getHeight() / 3);
        setTitle("Settings");
        setLocationRelativeTo(null);
        setResizable(false);
        initializeComponents();
    }

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public static void showWindow(Component parent) {
        Settings s = getInstance();
        s.setLocationRelativeTo(parent);
        s.setVisible(true);
        parent.setEnabled(false);
        Settings.parent = parent;
    }

    public static void hideWindow() {
        getInstance().setVisible(false);
        parent.setFocusable(true);
        parent.setEnabled(true);
        parent.requestFocus();
        ((JFrame) parent).toFront();
        parent.repaint();
    }

    public static boolean isOpen() {
        return instance != null && instance.isVisible();
    }

    /**
     * Close the Help Window
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
//                .addSetting("IDE-Theme:", createThemeChooser())
                .addSetting("Editor:", new JComboBox<>(new String[]{"Light", "Dark"}))
                .addItem("Editor")
                .addSetting(createFontChooserPanel(PropertyKey.EDITOR_FONT, new EditorPreview()))
                .addItem("Console")
                .addSetting(createFontChooserPanel(PropertyKey.CONSOLE_FONT, new ConsolePreview()))
                .addItem("View")
                .addSetting("Show Binary:", new JCheckBox())
                .addToComponent(this);
    }

    private JPanel createFontChooserPanel(PropertyKey key, AbstractPreviewPane previewPane) {
        JPanel panel = new JPanel(new BorderLayout());
        FontChooser fontChooser = new FontChooser(Preferences.getInstance().readFont(key), previewPane);
        fontChooser.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        fontChooser.addChangeListener(event -> {
            FontSelectionModel model = (FontSelectionModel) event.getSource();
            Preferences.getInstance().saveFont(key, model.getSelectedFont());
        });
        panel.add(fontChooser);
        return panel;
    }

    private JComponent createThemeChooser() {
        UIManager.LookAndFeelInfo[] plaf = UIManager.getInstalledLookAndFeels();
        List<UIManager.LookAndFeelInfo> loafs = new ArrayList<>();
        loafs.add(new LightLafInfo());
        loafs.add(new DarkLafInfo());
        loafs.addAll(Arrays.asList(plaf));
        JComboBox<UIManager.LookAndFeelInfo> comboBox = new JComboBox<>(loafs.toArray(UIManager.LookAndFeelInfo[]::new));
        comboBox.setEditable(false);
        comboBox.setRenderer(new LookAndFeelInfoCellRenderer());
        comboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                UIManager.LookAndFeelInfo info = (UIManager.LookAndFeelInfo) e.getItem();
                var pref = Preferences.getInstance();
                pref.saveString(PropertyKey.THEME, info.getName());
                pref.saveString(PropertyKey.THEME_PATH, info.getClassName());
            }
        });
        int index = 0;
        for (UIManager.LookAndFeelInfo info : loafs) {
            if (info.getName().equals(LafManager.getCurrentLaf())) {
                break;
            }
            index++;
        }
        comboBox.setSelectedIndex(index);
        return comboBox;
    }

    private JComponent createSyntaxChooser() {
        return null;
    }
}
