package edu.kit.mima.app;

import edu.kit.mima.gui.menu.Help;
import edu.kit.mima.gui.menu.MenuBuilder;
import edu.kit.mima.gui.menu.settings.Settings;
import edu.kit.mima.loading.FileManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.JMenuBar;
import javax.swing.UIManager;
import javax.swing.border.MatteBorder;

/**
 * MenuBar for Mima App.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class MimaMenuBar {

    private final MimaUserInterface mimaUI;
    private final FileActions fileActions;
    private final JMenuBar menuBar;

    /**
     * Menu Bar for mima App.
     *
     * @param mimaUI      parent app.
     * @param fileActions file loading/savings actions.
     */
    public MimaMenuBar(final MimaUserInterface mimaUI, final FileActions fileActions) {
        this.mimaUI = mimaUI;
        this.fileActions = fileActions;
        this.menuBar = createMenu();
    }

    /**
     * Setup the MenuBar with all of its components.
     */
    @NotNull
    private JMenuBar createMenu() {
        final var menu = new MenuBuilder()
                .addMenu("File").setMnemonic('F')
                .addItem("New", () -> fileActions
                        .openFile(FileManager::newFile), "control N")
                .addItem("Load", () -> fileActions
                        .openFile(FileManager::load), "control L")
                .separator()
                .addItem("Settings", () -> Settings.showWindow(mimaUI),
                         "control alt S")
                .separator()
                .addItem("Save", fileActions::saveSmart, "control S")
                .addItem("Save as", fileActions::saveAs, "control shift S")
                .addItem("Quit", mimaUI::quit)
                .addMenu("Edit").setMnemonic('E')
                .addItem("Undo", () -> mimaUI.currentEditor().undo())
                .addItem("Redo", () -> mimaUI.currentEditor().undo())
                .addMenu("Help").setMnemonic('H')
                .addItem("Show Help", () -> Help.showWindow(mimaUI))
                .get();
        menu.setBorder(new MatteBorder(0, 0, 1, 0,
                                       UIManager.getColor("Border.line1")));
        return menu;
    }

    /**
     * Get the menubar.
     *
     * @return themenu bar.
     */
    public JMenuBar getMenuBar() {
        return menuBar;
    }
}
