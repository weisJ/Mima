package edu.kit.mima.core.query.programQuery;

import edu.kit.mima.core.parsing.token.ProgramToken;
import edu.kit.mima.core.parsing.token.Token;
import edu.kit.mima.core.query.Query;
import edu.kit.mima.core.query.QueryResult;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class ProgramQuery implements Query<Token> {

    private final ProgramToken program;
    private Predicate<Token> filter;
    private BiFunction<Predicate<Token>, Predicate<Token>, Predicate<Token>> joiningFunction;

    public ProgramQuery(ProgramToken program) {
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
    /* default */ Predicate<Token> getFilter() {
        return filter;
    }

    /**
     * Set the current filter. Should only be called from ProgramQueryResult to keep behaviour
     * coherent
     *
     * @param filter new Filter
     */
    /* default */ void setFilter(Predicate<Token> filter) {
        this.filter = filter;
    }

    /**
     * Get the Tokens this query works with. Should only be called from ProgramQueryResult to keep behaviour
     * coherent
     *
     * @return ProgramToken
     */
    /* default */ ProgramToken getTokens() {
        return program;
    }

    /**
     * Set the joining function of this Query.
     * Should only be called from DataQueryResult to keep behaviour coherent.
     *
     * @param joiningFunction new Joining Function
     */
    /* default */ void setJoiningFunction(
            BiFunction<Predicate<Token>, Predicate<Token>, Predicate<Token>> joiningFunction) {
        this.joiningFunction = joiningFunction;
    }


    @Override
    public <K> QueryResult<Token> whereEqual(Function<Token, K> function, K compare) {
        filter = joiningFunction.apply(filter, item -> Objects.equals(function.apply(item), compare));
        return new ProgramQueryResult(this);
    }

    @Override
    public <K> QueryResult<Token> whereNotEqual(Function<Token, K> function, K compare) {
        filter = joiningFunction.apply(filter, item -> !Objects.equals(function.apply(item), compare));
        return new ProgramQueryResult(this);
    }

    @Override
    public QueryResult<Token> where(Predicate<Token> predicate) {
        filter = joiningFunction.apply(filter, predicate);
        return new ProgramQueryResult(this);
    }

    @Override
    public QueryResult<Token> whereNot(Predicate<Token> predicate) {
        filter = joiningFunction.apply(filter, predicate.negate());
        return new ProgramQueryResult(this);
    }

    @Override
    public QueryResult<Token> all() {
        return new ProgramQueryResult(this);
    }

    @Override
    public void reset() {
        this.filter = t -> true;
        this.joiningFunction = Predicate::and;
    }
}
