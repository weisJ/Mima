package edu.kit.mima.script.token;

import edu.kit.mima.core.file.FileObjectAdapter;
import edu.kit.mima.core.token.Token;
import edu.kit.mima.core.token.TokenType;
import edu.kit.mima.script.lang.ScriptKeyword;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class ConditionalToken extends FileObjectAdapter implements Token<Token<?>> {

    private static final Pattern INDENT = Pattern.compile("\n");
    private static final String INDENT_REPLACEMENT = "\n\t";

    private final Token<?> condition;
    private final Token<?> thenBody;
    private final Token<?> elseBody;

    public ConditionalToken(final Token<?> condition, final Token<?> thenBody, final Token<?> elseBody) {
        this.condition = condition;
        this.thenBody = thenBody;
        this.elseBody = elseBody;
    }

    public ConditionalToken(final Token<?> condition, final Token<?> thenBody) {
        this.condition = condition;
        this.thenBody = thenBody;
        this.elseBody = null;
    }

    public Token<?> getCondition() {
        return condition;
    }

    public Token<?> getThenBody() {
        return thenBody;
    }

    public Token<?> getElseBody() {
        return elseBody;
    }

    public boolean hasElseBody() {
        return elseBody != null;
    }

    @NotNull
    @Override
    public Token<?> getValue() {
        return condition;
    }

    @Override
    public @NotNull TokenType getType() {
        return TokenType.CONDITIONAL;
    }

    @Override
    public Stream<Token<?>> stream(final boolean includeChildren) {
        Stream<Token<?>> stream = Stream.of(this);
        if (includeChildren) {
            stream = Stream.concat(stream, condition.stream(true));
            stream = Stream.concat(stream, thenBody.stream(true));
            if (elseBody != null) {
                stream = Stream.concat(stream, elseBody.stream(true));
            }
        }
        return stream;
    }

    @Override
    public @NotNull String simpleName() {
        var str = new StringBuilder(ScriptKeyword.IF).append(' ');
        str.append('(').append(condition.simpleName()).append(") ");
        str.append(ScriptKeyword.THEN).append(" {\n");
        str.append(INDENT.matcher(thenBody.simpleName()).replaceAll(INDENT_REPLACEMENT)).append("\n}");
        if (elseBody != null) {
            str.append(ScriptKeyword.ELSE).append(" {\n");
            str.append(INDENT.matcher(elseBody.simpleName()).replaceAll(INDENT_REPLACEMENT)).append("\n}");
        }
        return str.toString();
    }

    @Override
    public String toString() {
        return "[type="
               + TokenType.CONDITIONAL
               + "] {\n\t"
               + INDENT.matcher(condition.toString()).replaceAll(INDENT_REPLACEMENT)
               + "\n\t"
               + INDENT.matcher(thenBody.toString()).replaceAll(INDENT_REPLACEMENT)
               + ((elseBody != null) ? ("\n\t"
                                        + INDENT.matcher(elseBody.toString()).replaceAll(INDENT_REPLACEMENT))
                                     : "")
               + "\n}";
    }

    @Override
    public int getLineIndex() {
        return 0;
    }

    @Override
    public int getOffset() {
        return 0;
    }


    @Override
    public int getLength() {
        return 0;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(@Nullable final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ConditionalToken that = (ConditionalToken) obj;
        return Objects.equals(condition, that.condition)
               && Objects.equals(thenBody, that.thenBody)
               && Objects.equals(elseBody, that.elseBody);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, thenBody, elseBody);
    }
}
