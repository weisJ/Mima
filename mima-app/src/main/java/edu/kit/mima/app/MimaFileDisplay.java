package edu.kit.mima.app;

import edu.kit.mima.App;
import edu.kit.mima.core.MimaConstants;
import edu.kit.mima.core.instruction.InstructionSet;
import edu.kit.mima.gui.components.folderdisplay.FilePathDisplay;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * File Display for Mima Component.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class MimaFileDisplay {

    @NotNull
    private final FilePathDisplay filePathDisplay;

    /**
     * Create File display component for Mima App.
     *
     * @param fileActions the file actions.
     */
    public MimaFileDisplay(@NotNull final FileActions fileActions) {
        filePathDisplay = new FilePathDisplay();
        filePathDisplay.setHandler(
                file -> {
                    filePathDisplay.requestFocus();
                    if (file.isDirectory()) {
                        filePathDisplay.setFile(file);
                        filePathDisplay.focusLast();
                    } else if (MimaConstants.instructionSetForFile(file) != InstructionSet.EMPTY) {
                        fileActions.openFile(
                                fm -> {
                                    try {
                                        fm.load(file.getAbsolutePath());
                                    } catch (@NotNull final IOException e) {
                                        App.logger.error(e.getMessage());
                                    }
                                });
                    }
                });
        filePathDisplay.setFile(new File(Preferences.getInstance().readString(PropertyKey.DIRECTORY_MIMA)));
    }

    /**
     * Get the display component.
     *
     * @return the file display component.
     */
    @NotNull
    public FilePathDisplay getDisplay() {
        return filePathDisplay;
    }
}
