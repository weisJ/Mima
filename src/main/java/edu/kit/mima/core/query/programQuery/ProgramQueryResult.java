package edu.kit.mima.core.query.programQuery;

import edu.kit.mima.core.parsing.token.ArrayToken;
import edu.kit.mima.core.parsing.token.BinaryToken;
import edu.kit.mima.core.parsing.token.ProgramToken;
import edu.kit.mima.core.parsing.token.Token;
import edu.kit.mima.core.parsing.token.TokenType;
import edu.kit.mima.core.query.IllegalRequestException;
import edu.kit.mima.core.query.Query;
import edu.kit.mima.core.query.QueryItem;
import edu.kit.mima.core.query.QueryResult;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class ProgramQueryResult implements QueryResult<Token> {

    private final ProgramQuery query;

    /**
     * Get the DataQuery result from a database-query.
     * Should only be called by methods in ProgramQuery
     *
     * @param query query request
     */
    /* default */ ProgramQueryResult(ProgramQuery query) {
        this.query = query;
    }

    /*
     * Commands used for filter chaining
     */
    @Override
    public Query<Token> and() {
        query.setJoiningFunction(Predicate::and);
        return query;
    }

    @Override
    public Query<Token> or() {
        query.setJoiningFunction(Predicate::or);
        return query;
    }

    @Override
    public QueryResult<Token> invert() {
        query.setFilter(query.getFilter().negate());
        return this;
    }

    /*
     * Commands used for fetching data and inspecting the query result
     */

    /**
     * {@inheritDoc }
     * Resets the query
     */
    @Override
    public List<Token> get() {
        var result = createTokenStream(query.getTokens())
                .filter(query.getFilter()).collect(Collectors.toList());
        query.reset();
        return result;
    }

    /**
     * {@inheritDoc }
     * Resets the query
     */
    @Override
    public Stream<Token> stream() {
        var stream = createTokenStream(query.getTokens())
                .filter(query.getFilter());
        query.reset();
        return stream;
    }

    /**
     * {@inheritDoc }
     * Resets the query
     */
    @Override
    public List<Token> getSorted(Comparator<? super Token> comparator) {
        var result = createTokenStream(query.getTokens())
                .filter(query.getFilter()).sorted(comparator).collect(Collectors.toList());
        query.reset();
        return result;
    }

    /**
     * {@inheritDoc }
     * Resets the query
     */
    @Override
    public void forEach(Consumer<Token> consumer) {
        createTokenStream(query.getTokens()).filter(query.getFilter()).forEach(consumer);
        query.reset();
    }

    @Override
    public QueryItem<Token> findFirst() {
        return createTokenStream(query.getTokens())
                .filter(query.getFilter())
                .findFirst()
                .map(ProgramQueryResult.ProgramQueryItem::new)
                .orElseGet(() -> new ProgramQueryResult.ProgramQueryItem(null));
    }

    /**
     * {@inheritDoc }
     * Resets the query
     */
    @Override
    public boolean isEmpty() {
        boolean match = !anyMatch();
        query.reset();
        return match;
    }

    /**
     * {@inheritDoc }
     * Resets the query
     */
    @Override
    public boolean anyMatch() {
        boolean match = createTokenStream(query.getTokens()).anyMatch(query.getFilter());
        query.reset();
        return match;
    }

    /*
     * Create Token stream from program Token flattening all nested program occurrences.
     */
    private Stream<Token> createTokenStream(ProgramToken programToken) {
        return Arrays.stream(programToken.getValue()).flatMap(this::mapToken);
    }

    @SuppressWarnings("unchecked") /*Construction of tokens guarantees these types*/
    private Stream<Token> mapToken(Token token) {
        switch (token.getType()) {
            case PROGRAM:
                return createTokenStream((ProgramToken) token);
            case JUMP_POINT:
                BinaryToken<Token, Token> binaryToken = (BinaryToken<Token, Token>) token;
                return Stream.concat(Stream.of(binaryToken), mapToken(binaryToken.getSecond()));
            case ARRAY:
                return Arrays.stream(((ArrayToken<Token>) token).getValue());
            case DEFINITION: /*fall through*/
            case CONSTANT:
                Token value = ((Token<Token>) token).getValue();
                if (value.getType() == TokenType.ARRAY) {
                    return mapToken(value);
                } else {
                    return Stream.of(token);
                }
            case IDENTIFICATION:
            case KEYWORD:
            case PUNCTUATION:
            case BINARY:
            case NUMBER:
            case EMPTY:
            case ERROR:
            case CALL:
                /*fall through*/
                return Stream.of(token);
            default:
                return Stream.empty();
        }
    }

    /**
     * QueryItem of a Database Query. Acts like an Optional object, but gives the ability
     * to add items to the original Database
     */
    private final class ProgramQueryItem implements QueryItem<Token> {

        private final Token optionalResult;
        private final boolean isPresent;

        private ProgramQueryItem(Token optionalResult) {
            isPresent = optionalResult != null;
            this.optionalResult = optionalResult;
        }

        /**
         * {@inheritDoc }
         * Resets the query
         */
        @Override
        public boolean exists() {
            query.reset();
            return isPresent;
        }

        /**
         * {@inheritDoc }
         * Resets the query
         */
        @Override
        public Token orElseAdd(Token item) {
            query.reset();
            throw new UnsupportedOperationException("Can't add to Program");
        }

        /**
         * {@inheritDoc }
         * Resets the query
         */
        @Override
        public Token get() throws IllegalRequestException {
            query.reset();
            if (isPresent) {
                return optionalResult;
            } else {
                throw new IllegalRequestException("No items found");
            }
        }

        /**
         * {@inheritDoc }
         * Resets the query
         */
        @Override
        public <X extends Throwable> Token orElseThrow(Supplier<X> exceptionSupplier) throws X {
            query.reset();
            if (isPresent) {
                return optionalResult;
            } else {
                throw exceptionSupplier.get();
            }
        }
    }
}
