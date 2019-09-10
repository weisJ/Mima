package edu.kit.mima.gui.components.console.terminal;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;

/**
 * Terminal interface.
 *
 * @author Jannis Weis
 * @since 2019
 */
public interface Terminal extends AutoCloseable {

    /**
     * Create OS dependent terminal session.
     *
     * @return the terminal instance for the current os.
     */
    @Nullable
    static Terminal createTerminal() {
        try {
            return new WindowsTerminal();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    JComponent getComponent();
}
