package edu.kit.mima.script.token;

import edu.kit.mima.core.token.ListToken;
import edu.kit.mima.core.token.ProgramToken;
import edu.kit.mima.core.token.Token;
import edu.kit.mima.core.token.TokenType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class FunctionToken extends ListToken<Token<?>> {


    private final String name;
    private final ProgramToken body;

    public FunctionToken(final String name, @NotNull final List<Token<?>> values, final ProgramToken body) {
        super(values, 0, 0);
        this.name = name;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public ProgramToken getBody() {
        return body;
    }

    @Override
    public @NotNull TokenType getType() {
        return TokenType.FUNCTION;
    }

    @Override
    public @NotNull String toString() {
        return "[type=function"
               + "] {\n\t"
               + "name = " + name + "\n\t"
               + "args = ["
               + getValue().stream()
                         .map(t -> t.getValue().toString())
                         .collect(Collectors.joining(", "))
               + "]\n\t"
               + "body = "
               + INDENT.matcher(body.toString()).replaceAll(INDENT_REPLACEMENT)
               + "\n}";
    }
}
