package edu.kit.mima.core.running;

import edu.kit.mima.core.CodeChecker;
import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.parsing.preprocessor.PreProcessor;
import edu.kit.mima.core.parsing.token.ProgramToken;
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
     * @return compiled {@link ProgramToken}.
     */
    public ProgramToken compile(@NotNull final String input) {
        return compile(input, true, true, true);
    }

    /**
     * Compile given input.
     *
     * @param input        input file
     * @param throwErrors  whether errors should be thrown or ignored.
     * @param preProcess   whether preprocessor statements should be processed.
     * @param performCheck whether code checking should be performed.
     * @return compiled {@link ProgramToken}.
     */
    public ProgramToken compile(@NotNull String input,
                                final boolean throwErrors,
                                final boolean preProcess,
                                final boolean performCheck) {
        final List<Exception> errors = new ArrayList<>();
        if (preProcess) {
            final var processed = new PreProcessor(input, true).process();
            errors.addAll(processed.getSecond());
            input = processed.getFirst();
        }
        final var parsed = new Parser(input).parse();
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
