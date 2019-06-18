package edu.kit.mima.core.query.programquery;

import edu.kit.mima.core.query.Query;
import edu.kit.mima.core.query.QueryResult;
import edu.kit.mima.core.token.ProgramToken;
import edu.kit.mima.core.token.Token;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Query for searching through a {@link ProgramToken}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class ProgramQuery implements Query<Token<?>> {

    private final ProgramToken program;
    private Predicate<Token<?>> filter;
    private BiFunction<Predicate<Token<?>>, Predicate<Token<?>>, Predicate<Token<?>>> joiningFunction;

    /**
     * Create query from program.
     *
     * @param program program to query.
     */
    @Contract(pure = true)
    public ProgramQuery(final ProgramToken program) {
        this.program = program;
        this.filter = t -> true;
        this.joiningFunction = Predicate::and;
    }

    /**
     * Get the current filter. Should only be called from ProgramQueryResult to keep behaviour
     * coherent
     *
     * @return current filter
     */
    /* default */ Predicate<Token<?>> getFilter() {
        return filter;
    }

    /**
     * Set the current filter. Should only be called from ProgramQueryResult to keep behaviour
     * coherent
     *
     * @param filter new Filter
     */
    /* default */ void setFilter(final Predicate<Token<?>> filter) {
        this.filter = filter;
    }

    /**
     * Get the Tokens this query works with. Should only be called from ProgramQueryResult to keep
     * behaviour coherent
     *
     * @return ProgramToken
     */
    /* default */ ProgramToken getTokens() {
        return program;
    }

    /**
     * Set the joining function of this Query. Should only be called from DataQueryResult to keep
     * behaviour coherent.
     *
     * @param joiningFunction new Joining Function
     */
    // @formatter:off
    /* default */ void setJoiningFunction(
            final BiFunction<Predicate<Token<?>>, Predicate<Token<?>>, Predicate<Token<?>>> joiningFunction) {
        // @formatter:on
        this.joiningFunction = joiningFunction;
    }

    @NotNull
    @Override
    public <K> QueryResult<Token<?>> whereEqual(
            @NotNull final Function<Token<?>, K> function, final K compare) {
        filter = joiningFunction.apply(filter, item -> Objects.equals(function.apply(item), compare));
        return new ProgramQueryResult(this);
    }

    @NotNull
    @Override
    public <K> QueryResult<Token<?>> whereNotEqual(
            @NotNull final Function<Token<?>, K> function, final K compare) {
        filter = joiningFunction.apply(filter, item -> !Objects.equals(function.apply(item), compare));
        return new ProgramQueryResult(this);
    }

    @NotNull
    @Override
    public QueryResult<Token<?>> where(final Predicate<Token<?>> predicate) {
        filter = joiningFunction.apply(filter, predicate);
        return new ProgramQueryResult(this);
    }

    @NotNull
    @Override
    public QueryResult<Token<?>> whereNot(@NotNull final Predicate<Token<?>> predicate) {
        filter = joiningFunction.apply(filter, predicate.negate());
        return new ProgramQueryResult(this);
    }

    @NotNull
    @Override
    public QueryResult<Token<?>> all() {
        return new ProgramQueryResult(this);
    }

    @Override
    public void reset() {
        this.filter = t -> true;
        this.joiningFunction = Predicate::and;
    }
}
