package edu.kit.mima.core.running;

import edu.kit.mima.core.CodeChecker;
import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.parsing.preprocessor.PreProcessor;
import edu.kit.mima.core.parsing.token.ProgramToken;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class MimaCompiler {

    private final List<CompilationEventHandler> compilationEventHandlers;

    public MimaCompiler() {
        compilationEventHandlers = new ArrayList<>();
    }

    public ProgramToken compile(String input) {
        return compile(input, true, true, true);
    }

    public ProgramToken compile(String input, boolean throwErrors, boolean preProcess, boolean performCheck) {
        List<Exception> errors = new ArrayList<>();
        if (preProcess) {
            var processed = new PreProcessor(input).process();
            errors.addAll(processed.getSecond());
            input = processed.getFirst();
        }
        var parsed = new Parser(input).parse();
        var programToken = parsed.getFirst();
        errors.addAll(parsed.getSecond());
        if (!errors.isEmpty() && throwErrors) {
            String message = errors.stream()
                    .map(Exception::getMessage)
                    .collect(Collectors.joining("\n"));
            throw new IllegalArgumentException("Invalid Tokens \n" + message);
        }
        if (performCheck) {
            checkCode(programToken);
        }
        for (var handler : compilationEventHandlers) {
            handler.notifyCompilation(programToken);
        }
        return programToken;
    }

    /**
     * Check Code for probable bugs
     *
     * @param programToken program to check
     */
    public void checkCode(ProgramToken programToken) {
        CodeChecker.checkCode(programToken);
    }

    public void addCompilationEventHandler(final CompilationEventHandler handler) {
        this.compilationEventHandlers.add(handler);
    }

    public boolean removeCompilationEventHandler(final CompilationEventHandler handler) {
        return this.compilationEventHandlers.remove(handler);
    }


}
