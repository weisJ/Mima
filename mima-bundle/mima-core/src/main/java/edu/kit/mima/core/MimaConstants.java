package edu.kit.mima.core;

import edu.kit.mima.core.instruction.InstructionSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Constants for the Mima language.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class MimaConstants {

    public static final String MIMA_EXTENSION = "mima";
    public static final String MIMA_X_EXTENSION = "mimax";
    public static final String[] EXTENSIONS = new String[]{MIMA_EXTENSION, MIMA_X_EXTENSION};

    @Contract(" -> fail")
    private MimaConstants() {
        assert false : "flied class";
    }

    /**
     * Get the associated Instruction set for a file.
     *
     * @param file the file.
     * @return Instructions set of {@link InstructionSet#EMPTY} if none is found.
     */
    @NotNull
    public static InstructionSet instructionSetForFile(@NotNull final File file) {
        return instructionSetForFile(file.getName());
    }

    /**
     * Get the associated Instruction set for a file.
     *
     * @param file path of file or filename.
     * @return Instructions set of {@link InstructionSet#EMPTY} if none is found.
     */
    @NotNull
    @Contract(pure = true)
    public static InstructionSet instructionSetForFile(@NotNull final String file) {
        if (file.endsWith("." + MimaConstants.MIMA_X_EXTENSION)) {
            return InstructionSet.MIMA_X;
        } else if (file.endsWith("." + MIMA_EXTENSION)) {
            return InstructionSet.MIMA;
        }
        return InstructionSet.EMPTY;
    }
}
