package edu.kit.mima.core;

import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.parsing.preprocessor.PreProcessor;
import edu.kit.mima.core.token.ProgramToken;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Compiler for Mima Code.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class MimaCompiler {

    /**
     * Compile given input.
     *
     * @param input file input.
     * @param workingDirectory working directory
     * @param mimaDirectory    mima directory
     * @return compiled {@link ProgramToken}.
     */
    public ProgramToken compile(@NotNull String input,
                                @NotNull String workingDirectory,
                                @NotNull String mimaDirectory) {
        return compile(input, workingDirectory, mimaDirectory, true, true, true);
    }

    /**
     * Compile given input.
     *
     * @param input            input file
     * @param workingDirectory working directory
     * @param mimaDirectory    mima directory
     * @param throwErrors      whether errors should be thrown or ignored.
     * @param preProcess       whether preprocessor statements should be processed.
     * @param performCheck     whether code checking should be performed.
     * @return compiled {@link ProgramToken}.
     */
    public ProgramToken compile(@NotNull String input,
                                @NotNull String workingDirectory,
                                @NotNull String mimaDirectory,
                                final boolean throwErrors,
                                final boolean preProcess,
                                final boolean performCheck) {
        final List<Exception> errors = new ArrayList<>();
        String text = input;
        if (preProcess) {
            final var processed = new PreProcessor(input, workingDirectory,
                                                   mimaDirectory, true).process();
            errors.addAll(processed.getSecond());
            text = processed.getFirst();
        }
        final var parsed = new Parser(text).parse();
        final var programToken = parsed.getFirst();
        errors.addAll(parsed.getSecond());
        if (!errors.isEmpty() && throwErrors) {
            final String message = errors.stream()
                    .map(Exception::getMessage)
                    .collect(Collectors.joining("\n"));
            throw new IllegalArgumentException("Invalid Tokens \n" + message);
        }
        if (performCheck) {
            checkCode(programToken);
        }
        return programToken;
    }

    /**
     * Check Code for probable bugs.
     *
     * @param programToken program to check
     */
    private void checkCode(final ProgramToken programToken) {
        CodeChecker.checkCode(programToken);
    }
}
