package edu.kit.mima.core;

import edu.kit.mima.core.query.programquery.ProgramQuery;
import edu.kit.mima.core.token.ProgramToken;
import edu.kit.mima.core.token.Token;
import edu.kit.mima.core.token.TokenType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
        List<String> warnings = new ArrayList<>(checkReferenceDuplicates(token));
        var logger = MimaCoreDefaults.getLogger();
        for (String s : warnings) {
            logger.warning(s);
        }
    }

    /**
     * Search for duplicate reference declarations. this may be a bug in the program and results in
     * not expected behaviour.
     *
     * @param token Program token to check
     */
    @NotNull
    @SuppressWarnings("unchecked") /*Construction of tokens guarantees these
    types*/
    private static List<String> checkReferenceDuplicates(final ProgramToken token) {
        final List<String> warnings = new ArrayList<>();
        final ProgramQuery query = new ProgramQuery(token);
        final List<String> referencesJump =
                query.whereEqual(Token::getType, TokenType.JUMP_POINT).stream()
                        .map(t -> ((Token<Token<?>>) t).getValue().getValue().toString())
                        .collect(Collectors.toList());
        addWarnings(warnings, referencesJump);
        final List<String> referencesVar =
                query.whereEqual(Token::getType, TokenType.CONSTANT).or()
                        .whereEqual(Token::getType, TokenType.REFERENCE).stream()
                        .map(t -> ((Token<Token<?>>) t).getValue().getValue().toString())
                        .collect(Collectors.toList());
        addWarnings(warnings, referencesVar);
        return warnings;
    }

    private static void addWarnings(
            @NotNull final List<String> warnings, @NotNull final List<String> checkList) {
        Set<String> duplicates = findDuplicates(checkList);
        if (!duplicates.isEmpty()) {
            final String duplicatesS = String.join(", ", duplicates);
            warnings.add("Multiple Definitions: \"" + duplicatesS + '\"');
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
