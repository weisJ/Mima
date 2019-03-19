package edu.kit.mima.core;

import edu.kit.mima.core.parsing.token.ProgramToken;
import edu.kit.mima.core.parsing.token.Token;
import edu.kit.mima.core.parsing.token.TokenType;
import edu.kit.mima.core.query.programquery.ProgramQuery;
import edu.kit.mima.logging.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class to check Mima Code for probable bugs.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class CodeChecker {

    @Contract(" -> fail")
    private CodeChecker() {
        assert false : "utility class constructor";
    }

    /**
     * Check Code for probable bugs.
     *
     * @param token Program token to check
     */
    public static void checkCode(@Nullable final ProgramToken token) {
        if (token == null) {
            return;
        }
        checkReferenceDuplicates(token);
        checkNonCalls(token);
    }

    /**
     * Search for duplicate reference declarations. this may be a bug in the program and results in
     * not expected behaviour.
     *
     * @param token Program token to check
     */
    @SuppressWarnings("unchecked") /*Construction of tokens guarantees these types*/
    private static void checkReferenceDuplicates(final ProgramToken token) {
        final ProgramQuery query = new ProgramQuery(token);
        final List<String> referencesJump = query
                .whereEqual(Token::getType, TokenType.JUMP_POINT)
                .stream()
                .map(t -> ((Token<Token>) t).getValue().getValue().toString())
                .collect(Collectors.toList());
        Set<String> duplicates = findDuplicates(referencesJump);
        if (!duplicates.isEmpty()) {
            final String duplicatesS = String.join(", ", duplicates);
            Logger.warning("Multiple Jump Definitions: \"" + duplicatesS + '\"');
        }
        final List<String> referencesVar = query
                .whereEqual(Token::getType, TokenType.CONSTANT)
                .or()
                .whereEqual(Token::getType, TokenType.DEFINITION)
                .stream()
                .map(t -> ((Token<Token>) t).getValue().getValue().toString())
                .collect(Collectors.toList());
        duplicates = findDuplicates(referencesVar);
        if (!duplicates.isEmpty()) {
            final String duplicatesS = String.join(", ", duplicates);
            Logger.warning("Multiple Reference Definitions: \"" + duplicatesS + '\"');
        }
    }

    /**
     * Search for program statements that are not function calls.
     *
     * @param token program token to check
     */
    private static void checkNonCalls(final ProgramToken token) {
        final List<Token> nonCalls = new ProgramQuery(token)
                .whereNotEqual(Token::getType, TokenType.DEFINITION)
                .and()
                .whereNotEqual(Token::getType, TokenType.CONSTANT)
                .and()
                .whereNotEqual(Token::getType, TokenType.CALL)
                .and()
                .whereNotEqual(Token::getType, TokenType.JUMP_POINT).get();
        for (final Token t : nonCalls) {
            Logger.warning("not a function call: \"" + t.simpleName() + '\"');
        }
    }

    @NotNull
    private static <T> Set<T> findDuplicates(@NotNull final Collection<T> collection) {
        final Set<T> duplicates = new HashSet<>();
        final Set<T> uniques = new HashSet<>();
        for (final T t : collection) {
            if (!uniques.add(t)) {
                duplicates.add(t);
            }
        }
        return duplicates;
    }
}
