package edu.kit.mima.core.token;

import edu.kit.mima.core.file.FileObjectAdapter;
import edu.kit.mima.core.query.programquery.ProgramQuery;
import edu.kit.mima.core.query.programquery.ProgramQueryResult;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of {@link Token} that holds an array of {@link Token}s representing a program.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class ProgramToken extends FileObjectAdapter implements Token<Token<?>[]> {

    private static final Pattern INDENT = Pattern.compile("\n");
    private static final String INDENT_REPLACEMENT = "\n\t";
    private final List<Integer> indexList;
    private final int filePos;
    private final int index;
    private final int length;
    @NotNull
    private final Map<Token<?>, Integer> jumpMap;
    @NotNull
    private final Token<?>[] program;

    /**
     * Program token that holds an array of Tokens.
     *
     * @param program token array
     * @param indexList list with indices corresponding to the instruction index of each token.
     * @param index index of token.
     * @param length amount of statement tokens inside.
     * @param filePos position in file
     */
    public ProgramToken(@NotNull final Token<?>[] program, final List<Integer> indexList,
                        final int index, final int length, final int filePos) {
        this.program = program;
        this.indexList = indexList;
        this.filePos = filePos;
        this.index = index;
        this.length = length;
        jumpMap = new HashMap<>();
        resolveJumps();
    }

    /*
     * Create jump map for program token
     */
    private void resolveJumps() {
        final List<Token<?>> tokens =
                ((ProgramQueryResult) new ProgramQuery(this).whereEqual(Token::getType, TokenType.JUMP_POINT))
                        .get(false);
        for (final var token : tokens) {
            jumpMap.put((Token<?>) token.getValue(), token.getLineIndex());
        }
    }

    public List<Integer> getIndexList() {
        return indexList;
    }

    /**
     * Returns the jump associations for this program token.
     *
     * @return Map that with tokens as key and their program index as value
     */
    @NotNull
    public Map<Token<?>, Integer> getJumps() {
        return jumpMap;
    }

    @NotNull
    @Override
    public Token<?>[] getValue() {
        return program;
    }

    @NotNull
    @Override
    public TokenType getType() {
        return TokenType.PROGRAM;
    }

    @Override
    public Stream<Token<?>> stream(final boolean includeChildren) {
        if (includeChildren) {
            return Arrays.stream(program).flatMap(t -> t.stream(true));
        } else {
            return Stream.of(this);
        }
    }

    @Override
    public int getLineIndex() {
        return index;
    }

    @Override
    public int getOffset() {
        return filePos;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return print(Token::toString, "[prog]");
    }

    @NotNull
    @Override
    public String simpleName() {
        return print(Token::simpleName, "");
    }

    private String print(@NotNull final Function<Token<?>, String> mapping, final String prefix) {
        return Arrays.stream(program)
                       .map(t -> '\t' + INDENT.matcher(mapping.apply(t)).replaceAll(INDENT_REPLACEMENT) + '\n')
                       .collect(Collectors.joining("", prefix + "{\n", "}"));
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
        final ProgramToken that = (ProgramToken) obj;
        return Arrays.equals(program, that.program);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(program);
    }
}
