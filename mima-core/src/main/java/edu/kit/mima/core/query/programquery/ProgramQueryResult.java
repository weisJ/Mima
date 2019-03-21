package edu.kit.mima.core.query.programquery;

import edu.kit.mima.core.query.IllegalRequestException;
import edu.kit.mima.core.query.Query;
import edu.kit.mima.core.query.QueryItem;
import edu.kit.mima.core.query.QueryResult;
import edu.kit.mima.core.token.ArrayToken;
import edu.kit.mima.core.token.BinaryToken;
import edu.kit.mima.core.token.ProgramToken;
import edu.kit.mima.core.token.Token;
import edu.kit.mima.core.token.TokenType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Result from {@link ProgramQuery}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class ProgramQueryResult implements QueryResult<Token> {

    private final ProgramQuery query;

    /**
     * Get the DataQuery result from a database-query. Should only be called by methods in
     * ProgramQuery.
     *
     * @param query query request
     */
    /* default */ ProgramQueryResult(final ProgramQuery query) {
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

    @NotNull
    @Override
    public QueryResult<Token> invert() {
        query.setFilter(query.getFilter().negate());
        return this;
    }

    /*
     * Commands used for fetching data and inspecting the query result
     */

    /**
     * {@inheritDoc } Resets the query
     */
    @Override
    public List<Token> get() {
        return get(true);
    }

    /**
     * Return query result.
     *
     * @param recursive whether to recursively include program tokens
     * @return List of tokens matching query
     */
    public List<Token> get(final boolean recursive) {
        final var result = createTokenStream(query.getTokens(), recursive)
                .filter(query.getFilter()).collect(Collectors.toList());
        query.reset();
        return result;
    }

    /**
     * {@inheritDoc } Resets the query.
     */
    @Override
    public Stream<Token> stream() {
        final var stream = createTokenStream(query.getTokens(), true)
                .filter(query.getFilter());
        query.reset();
        return stream;
    }

    /**
     * {@inheritDoc } Resets the query.
     */
    @Override
    public List<Token> getSorted(final Comparator<? super Token> comparator) {
        final var result = createTokenStream(query.getTokens(), true)
                .filter(query.getFilter()).sorted(comparator).collect(Collectors.toList());
        query.reset();
        return result;
    }

    /**
     * {@inheritDoc } Resets the query.
     */
    @Override
    public void forEach(final Consumer<Token> consumer) {
        createTokenStream(query.getTokens(), true).filter(query.getFilter()).forEach(consumer);
        query.reset();
    }

    @Override
    public QueryItem<Token> findFirst() {
        return createTokenStream(query.getTokens(), true)
                .filter(query.getFilter())
                .findFirst()
                .map(ProgramQueryResult.ProgramQueryItem::new)
                .orElseGet(() -> new ProgramQueryResult.ProgramQueryItem(null));
    }

    /**
     * {@inheritDoc } Resets the query.
     */
    @Override
    public boolean isEmpty() {
        final boolean match = !anyMatch();
        query.reset();
        return match;
    }

    /**
     * {@inheritDoc } Resets the query.
     */
    @Override
    public boolean anyMatch() {
        final boolean match = createTokenStream(query.getTokens(), true)
                .anyMatch(query.getFilter());
        query.reset();
        return match;
    }

    /*
     * Create Token stream from program Token flattening all nested program occurrences.
     */
    private Stream<Token> createTokenStream(@NotNull final ProgramToken programToken,
                                            final boolean recursive) {
        return Arrays.stream(programToken.getValue()).flatMap(t -> mapToken(t, recursive));
    }

    @SuppressWarnings("unchecked") /*Construction of tokens guarantees these types*/
    private Stream<Token> mapToken(@NotNull final Token token, final boolean recursive) {
        switch (token.getType()) {
            case PROGRAM:
                if (recursive) {
                    return createTokenStream((ProgramToken) token, true);
                } else {
                    return Stream.empty();
                }
            case JUMP_POINT:
                final BinaryToken<Token, Token> binaryToken = (BinaryToken<Token, Token>) token;
                return Stream.concat(Stream.of(binaryToken),
                                     mapToken(binaryToken.getSecond(), recursive));
            case ARRAY:
                return Arrays.stream(((ArrayToken<Token>) token).getValue());
            case DEFINITION: /*fall through*/
            case CONSTANT:
                final Token value = ((Token<Token>) token).getValue();
                if (value.getType() == TokenType.ARRAY) {
                    return mapToken(value, recursive);
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
     * QueryItem of a Database Query. Acts like an Optional object, but gives the ability to add
     * items to the original Database
     */
    private final class ProgramQueryItem implements QueryItem<Token> {

        @Nullable
        private final Token optionalResult;
        private final boolean isPresent;

        private ProgramQueryItem(@Nullable final Token optionalResult) {
            isPresent = optionalResult != null;
            this.optionalResult = optionalResult;
        }

        /**
         * {@inheritDoc } Resets the query
         */
        @Override
        public boolean exists() {
            query.reset();
            return isPresent;
        }

        /**
         * {@inheritDoc } Resets the query
         */
        @NotNull
        @Override
        public Token orElseAdd(final Token item) {
            query.reset();
            throw new UnsupportedOperationException("Can't add to Program");
        }

        /**
         * {@inheritDoc } Resets the query
         */
        @Nullable
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
         * {@inheritDoc } Resets the query
         */
        @Nullable
        @Override
        public <X extends Throwable> Token orElseThrow(@NotNull final Supplier<X> exceptionSupplier)
                throws X {
            query.reset();
            if (isPresent) {
                return optionalResult;
            } else {
                throw exceptionSupplier.get();
            }
        }
    }
}