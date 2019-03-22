package edu.kit.mima.app;

import edu.kit.mima.App;
import edu.kit.mima.core.MimaConstants;
import edu.kit.mima.core.instruction.InstructionSet;
import edu.kit.mima.gui.components.folderdisplay.FileDisplay;
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

    @NotNull private final FileDisplay fileDisplay;

    /**
     * Create File display component for Mima App.
     *
     * @param fileActions the file actions.
     */
    public MimaFileDisplay(final FileActions fileActions) {
        fileDisplay = new FileDisplay();
        fileDisplay.setHandler(file -> {
            fileDisplay.requestFocus();
            if (file.isDirectory()) {
                fileDisplay.setFile(file);
                fileDisplay.focusLast();
            } else if (MimaConstants.instructionSetForFile(file) != InstructionSet.EMPTY) {
                fileActions.openFile(fm -> {
                    try {
                        fm.load(file.getAbsolutePath());
                    } catch (final IOException e) {
                        App.logger.error(e.getMessage());
                    }
                });
            }
        });
        fileDisplay.setFile(
                new File(Preferences.getInstance().readString(PropertyKey.DIRECTORY_MIMA)));
    }

    /**
     * Get the display component.
     *
     * @return the file display component.
     */
    public FileDisplay getDisplay() {
        return fileDisplay;
    }
}
