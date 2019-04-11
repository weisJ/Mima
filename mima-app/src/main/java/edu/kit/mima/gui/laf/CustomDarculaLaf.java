package edu.kit.mima.gui.laf;

import com.bulenkov.darcula.DarculaLaf;
import com.bulenkov.darcula.DarculaMetalTheme;
import com.bulenkov.iconloader.IconLoader;
import com.bulenkov.iconloader.util.ColorUtil;
import com.bulenkov.iconloader.util.EmptyIcon;
import com.bulenkov.iconloader.util.StringUtil;
import com.bulenkov.iconloader.util.SystemInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.awt.AppContext;

import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.IconUIResource;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.basic.BasicLookAndFeel;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CustomDarcula Look and Feel to allow for better extension.
 */
@SuppressWarnings({"CheckStyle"})
public class CustomDarculaLaf extends BasicLookAndFeel {
    private static final Logger LOGGER = Logger.getLogger(CustomDarculaLaf.class.getName());
    private static final String NAME = "Darcula";
    private BasicLookAndFeel base;

    /**
     * Create Custom Darcula LaF.
     */
    public CustomDarculaLaf() {
        try {
            if (SystemInfo.isWindows || SystemInfo.isLinux) {
                base = new MetalLookAndFeel();
                MetalLookAndFeel.setCurrentTheme(new DarculaMetalTheme());
            } else {
                final String name = UIManager.getSystemLookAndFeelClassName();
                base = (BasicLookAndFeel) Class.forName(name).getDeclaredConstructor().newInstance();
            }
        } catch (@NotNull final Exception e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
    }

    private static void patchComboBox(@NotNull final UIDefaults metalDefaults,
                                      @NotNull final UIDefaults defaults) {
        defaults.remove("ComboBox.ancestorInputMap");
        defaults.remove("ComboBox.actionMap");
        defaults.put("ComboBox.ancestorInputMap", metalDefaults.get("ComboBox.ancestorInputMap"));
        defaults.put("ComboBox.actionMap", metalDefaults.get("ComboBox.actionMap"));
    }

    private static void patchStyledEditorKit() {
        try {
            final StyleSheet defaultStyles = new StyleSheet();
            final InputStream is = DarculaLaf.class.getResourceAsStream("darcula.css");
            final Reader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            defaultStyles.loadRules(r, null);
            r.close();
            final Field keyField = HTMLEditorKit.class.getDeclaredField("DEFAULT_STYLES_KEY");
            keyField.setAccessible(true);
            final Object key = keyField.get(null);
            AppContext.getAppContext().put(key, defaultStyles);
            is.close();
        } catch (@NotNull final Throwable e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
    }

    @Nullable
    private static Object parseValue(@NotNull final String key, @NotNull final String value) {
        if ("null".equals(value)) {
            return null;
        }
        if (key.endsWith("Insets")) {
            final List<String> numbers = StringUtil.split(value, ",");
            return new InsetsUIResource(Integer.parseInt(numbers.get(0)),
                                        Integer.parseInt(numbers.get(1)),
                                        Integer.parseInt(numbers.get(2)),
                                        Integer.parseInt(numbers.get(3)));
        } else if (key.endsWith(".border")) {
            try {
                return Class.forName(value).getDeclaredConstructor().newInstance();
            } catch (@NotNull final Exception e) {
                LOGGER.log(Level.SEVERE, e.toString(), e);
            }
        } else if (key.endsWith(".font")) {
            try {
                final String[] decode = value.split("-");
                //noinspection MagicConstant
                return new Font(decode[0], Integer.parseInt(decode[1]), Integer.parseInt(decode[2]));
            } catch (@NotNull final Exception e) {
                return new Font("Monospaced", Font.PLAIN, 12);
            }
        } else {
            final Color color = ColorUtil.fromHex(value, null);
            final Integer invVal = getInteger(value);
            final Boolean boolVal = "true".equals(value)
                                    ? Boolean.TRUE
                                    : "false".equals(value) ? Boolean.FALSE : null;
            //TODO: copy image loading
            //
            // final Icon icon = key.toLowerCase().endsWith("icon") ? null : null;
            if (color != null) {
                return new ColorUIResource(color);
            } else if (invVal != null) {
                return invVal;
            } else if (boolVal != null) {
                return boolVal;
            }
        }
        return value;
    }

    @Nullable
    private static Integer getInteger(@NotNull final String value) {
        try {
            return Integer.parseInt(value);
        } catch (@NotNull final NumberFormatException ignored) {
            return null;
        }
    }

    @SuppressWarnings({"HardCodedStringLiteral"})
    private static void initInputMapDefaults(final UIDefaults defaults) {
        // Make ENTER work in JTrees
        final InputMap treeInputMap = (InputMap) defaults.get("Tree.focusInputMap");
        if (treeInputMap != null) {
            // it's really possible. For example,  GTK+ doesn't have such map
            treeInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "toggle");
        }
        // Cut/Copy/Paste in JTextAreas
        final InputMap textAreaInputMap = (InputMap) defaults.get("TextArea.focusInputMap");
        if (textAreaInputMap != null) {
            // It really can be null, for example when LAF isn't properly initialized
            // (Alloy license problem)
            installCutCopyPasteShortcuts(textAreaInputMap, false);
        }
        // Cut/Copy/Paste in JTextFields
        final InputMap textFieldInputMap = (InputMap) defaults.get("TextField.focusInputMap");
        if (textFieldInputMap != null) {
            // It really can be null, for example when LAF isn't properly initialized
            // (Alloy license problem)
            installCutCopyPasteShortcuts(textFieldInputMap, false);
        }
        // Cut/Copy/Paste in JPasswordField
        final InputMap passwordFieldInputMap = (InputMap) defaults
                .get("PasswordField.focusInputMap");
        if (passwordFieldInputMap != null) {
            // It really can be null, for example when LAF isn't properly initialized
            // (Alloy license problem)
            installCutCopyPasteShortcuts(passwordFieldInputMap, false);
        }
        // Cut/Copy/Paste in JTables
        final InputMap tableInputMap = (InputMap) defaults.get("Table.ancestorInputMap");
        if (tableInputMap != null) {
            // It really can be null, for example when LAF isn't properly initialized
            // (Alloy license problem)
            installCutCopyPasteShortcuts(tableInputMap, true);
        }
    }

    private static void installCutCopyPasteShortcuts(@NotNull final InputMap inputMap,
                                                     final boolean useSimpleActionKeys) {
        final String copyActionKey = useSimpleActionKeys ? "copy" : DefaultEditorKit.copyAction;
        final String pasteActionKey = useSimpleActionKeys ? "paste" : DefaultEditorKit.pasteAction;
        final String cutActionKey = useSimpleActionKeys ? "cut" : DefaultEditorKit.cutAction;
        // Ctrl+Ins, Shift+Ins, Shift+Del
        inputMap.put(
                KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.CTRL_DOWN_MASK),
                copyActionKey);
        inputMap.put(
                KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.SHIFT_DOWN_MASK),
                pasteActionKey);
        inputMap.put(
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.SHIFT_DOWN_MASK),
                cutActionKey);
        // Ctrl+C, Ctrl+V, Ctrl+X
        inputMap.put(
                KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK),
                copyActionKey);
        inputMap.put(
                KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK),
                pasteActionKey);
        inputMap.put(
                KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK),
                DefaultEditorKit.cutAction);
    }

    @NotNull
    protected String getPrefix() {
        return "darcula";
    }

    private void callInit(@NotNull final String method, final UIDefaults defaults) {
        try {
            final Method superMethod = BasicLookAndFeel.class
                    .getDeclaredMethod(method, UIDefaults.class);
            superMethod.setAccessible(true);
            superMethod.invoke(base, defaults);
        } catch (@NotNull final Exception e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
    }

    @Override
    public UIDefaults getDefaults() {
        try {
            final Method superMethod = BasicLookAndFeel.class.getDeclaredMethod("getDefaults");
            superMethod.setAccessible(true);
            final UIDefaults metalDefaults =
                    (UIDefaults) superMethod.invoke(new MetalLookAndFeel());
            final UIDefaults defaults = (UIDefaults) superMethod.invoke(base);
            initInputMapDefaults(defaults);
            initIdeaDefaults(defaults);
            patchStyledEditorKit();
            patchComboBox(metalDefaults, defaults);
            defaults.remove("Spinner.arrowButtonBorder");
            defaults.put("Spinner.arrowButtonSize", new Dimension(16, 5));
            defaults.put("Tree.collapsedIcon", new IconUIResource(IconLoader.getIcon(
                    "/com/bulenkov/darcula/icons/treeNodeCollapsed.png")));
            defaults.put("Tree.expandedIcon", new IconUIResource(IconLoader.getIcon(
                    "/com/bulenkov/darcula/icons/treeNodeExpanded.png")));
            defaults.put("Menu.arrowIcon", new IconUIResource(IconLoader.getIcon(
                    "/com/bulenkov/darcula/icons/menuItemArrowIcon.png")));
            defaults.put("CheckBoxMenuItem.checkIcon", EmptyIcon.create(16));
            defaults.put("RadioButtonMenuItem.checkIcon", EmptyIcon.create(16));
            defaults.put("InternalFrame.icon", new IconUIResource(IconLoader.getIcon(
                    "/com/bulenkov/darcula/icons/internalFrame.png")));
            defaults.put("OptionPane.informationIcon", new IconUIResource(IconLoader.getIcon(
                    "/com/bulenkov/darcula/icons/option_pane_info.png")));
            defaults.put("OptionPane.questionIcon", new IconUIResource(IconLoader.getIcon(
                    "/com/bulenkov/darcula/icons/option_pane_question.png")));
            defaults.put("OptionPane.warningIcon", new IconUIResource(IconLoader.getIcon(
                    "/com/bulenkov/darcula/icons/option_pane_warning.png")));
            defaults.put("OptionPane.errorIcon", new IconUIResource(IconLoader.getIcon(
                    "/com/bulenkov/darcula/icons/option_pane_error.png")));
            if (SystemInfo.isMac && !"true".equalsIgnoreCase(System.getProperty(
                    "apple.laf.useScreenMenuBar", "false"))) {
                defaults.put("MenuBarUI", "com.bulenkov.darcula.ui.DarculaMenuBarUI");
                defaults.put("MenuUI", "javax.swing.plaf.basic.BasicMenuUI");
            }
            return defaults;
        } catch (@NotNull final Exception e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
        return super.getDefaults();
    }

    private void call(@NotNull final String method) {
        try {
            final Method superMethod = BasicLookAndFeel.class.getDeclaredMethod(method);
            superMethod.setAccessible(true);
            superMethod.invoke(base);
        } catch (@NotNull final Exception e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
    }

    public void initComponentDefaults(final UIDefaults defaults) {
        callInit("initComponentDefaults", defaults);
    }

    @SuppressWarnings({"HardCodedStringLiteral"})
    private void initIdeaDefaults(@NotNull final UIDefaults defaults) {
        loadDefaults(defaults);
        defaults.put("Table.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{
                "ctrl C", "copy",
                "ctrl V", "paste",
                "ctrl X", "cut",
                "COPY", "copy",
                "PASTE", "paste",
                "CUT", "cut",
                "control INSERT", "copy",
                "shift INSERT", "paste",
                "shift DELETE", "cut",
                "RIGHT", "selectNextColumn",
                "KP_RIGHT", "selectNextColumn",
                "LEFT", "selectPreviousColumn",
                "KP_LEFT", "selectPreviousColumn",
                "DOWN", "selectNextRow",
                "KP_DOWN", "selectNextRow",
                "UP", "selectPreviousRow",
                "KP_UP", "selectPreviousRow",
                "shift RIGHT", "selectNextColumnExtendSelection",
                "shift KP_RIGHT", "selectNextColumnExtendSelection",
                "shift LEFT", "selectPreviousColumnExtendSelection",
                "shift KP_LEFT", "selectPreviousColumnExtendSelection",
                "shift DOWN", "selectNextRowExtendSelection",
                "shift KP_DOWN", "selectNextRowExtendSelection",
                "shift UP", "selectPreviousRowExtendSelection",
                "shift KP_UP", "selectPreviousRowExtendSelection",
                "PAGE_UP", "scrollUpChangeSelection",
                "PAGE_DOWN", "scrollDownChangeSelection",
                "HOME", "selectFirstColumn",
                "END", "selectLastColumn",
                "shift PAGE_UP", "scrollUpExtendSelection",
                "shift PAGE_DOWN", "scrollDownExtendSelection",
                "shift HOME", "selectFirstColumnExtendSelection",
                "shift END", "selectLastColumnExtendSelection",
                "ctrl PAGE_UP", "scrollLeftChangeSelection",
                "ctrl PAGE_DOWN", "scrollRightChangeSelection",
                "ctrl HOME", "selectFirstRow",
                "ctrl END", "selectLastRow",
                "ctrl shift PAGE_UP", "scrollRightExtendSelection",
                "ctrl shift PAGE_DOWN", "scrollLeftExtendSelection",
                "ctrl shift HOME", "selectFirstRowExtendSelection",
                "ctrl shift END", "selectLastRowExtendSelection",
                "TAB", "selectNextColumnCell",
                "shift TAB", "selectPreviousColumnCell",
                //"ENTER", "selectNextRowCell",
                "shift ENTER", "selectPreviousRowCell",
                "ctrl A", "selectAll",
                "meta A", "selectAll",
                //"ESCAPE", "cancel",
                "F2", "startEditing"
        }));
    }

    private void loadDefaults(@NotNull final UIDefaults defaults) {
        final Properties properties = new Properties();
        final String osSuffix = SystemInfo.isMac
                                ? "mac" : SystemInfo.isWindows ? "windows" : "linux";
        try {
            InputStream stream = CustomDarculaLaf.class
                    .getResourceAsStream(getPrefix() + ".properties");
            properties.load(stream);
            stream.close();

            stream = CustomDarculaLaf.class
                    .getResourceAsStream(getPrefix() + "_" + osSuffix + ".properties");
            properties.load(stream);
            stream.close();

            final HashMap<String, Object> darculaGlobalSettings = new HashMap<>();
            final String prefix = "darcula.";
            for (final String key : properties.stringPropertyNames()) {
                if (key.startsWith(prefix)) {
                    darculaGlobalSettings
                            .put(key.substring(prefix.length()),
                                 parseValue(key, properties.getProperty(key)));
                }
            }

            for (final Object key : defaults.keySet()) {
                if (key instanceof String && ((String) key).contains(".")) {
                    final String s = (String) key;
                    final String darculaKey = s.substring(s.lastIndexOf('.') + 1);
                    if (darculaGlobalSettings.containsKey(darculaKey)) {
                        defaults.put(key, darculaGlobalSettings.get(darculaKey));
                    }
                }
            }

            for (final String key : properties.stringPropertyNames()) {
                final String value = properties.getProperty(key);
                defaults.put(key, parseValue(key, value));
            }

            //CustomUI classes.
            defaults.put("EditorTabbedPane",
                         "edu.kit.mima.gui.laf.components.DarculaEditorTabbedPaneUI");
        } catch (@NotNull final IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
    }

    @NotNull
    @Override
    public String getName() {
        return NAME;
    }

    @NotNull
    @Override
    public String getID() {
        return getName();
    }

    @NotNull
    @Override
    public String getDescription() {
        return "IntelliJ Dark Look and Feel";
    }

    @Override
    public boolean isNativeLookAndFeel() {
        return true;
    }

    @Override
    public boolean isSupportedLookAndFeel() {
        return true;
    }

    @Override
    protected void initSystemColorDefaults(final UIDefaults defaults) {
        callInit("initSystemColorDefaults", defaults);
    }

    @Override
    protected void initClassDefaults(final UIDefaults defaults) {
        callInit("initClassDefaults", defaults);
    }

    @Override
    public void initialize() {
        call("initialize");
    }

    @Override
    public void uninitialize() {
        call("uninitialize");
    }

    @Override
    protected void loadSystemColors(final UIDefaults defaults, final String[] systemColors,
                                    final boolean useNative) {
        try {
            final Method superMethod = BasicLookAndFeel.class.getDeclaredMethod("loadSystemColors",
                                                                                UIDefaults.class,
                                                                                String[].class,
                                                                                boolean.class);
            superMethod.setAccessible(true);
            superMethod.invoke(base, defaults, systemColors, useNative);
        } catch (@NotNull final Exception e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
    }

    @Override
    public boolean getSupportsWindowDecorations() {
        return true;
    }
}
