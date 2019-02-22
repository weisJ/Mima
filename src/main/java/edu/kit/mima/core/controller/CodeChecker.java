package edu.kit.mima.core.controller;

import edu.kit.mima.core.parsing.token.ProgramToken;
import edu.kit.mima.core.parsing.token.Token;
import edu.kit.mima.core.parsing.token.TokenType;
import edu.kit.mima.core.query.programQuery.ProgramQuery;
import edu.kit.mima.gui.logging.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jannis Weis
 * @since 2018
 */
public final class CodeChecker {

    private CodeChecker() {
        assert false : "utility class constructor";
    }

    /**
     * Check Code for probable bugs
     *
     * @param token Program token to check
     */
    public static void checkCode(ProgramToken token) {
        if (token == null) {
            return;
        }
        checkReferenceDuplicates(token);
        checkNonCalls(token);
    }

    /**
     * Search for duplicate reference declarations. this may be a bug in the program and results in not expected behaviour
     *
     * @param token Program token to check
     */
    private static void checkReferenceDuplicates(ProgramToken token) {
        ProgramQuery query = new ProgramQuery(token);
        List<String> referencesJump = query
                .whereEqual(Token::getType, TokenType.JUMP_POINT).get()
                .stream()
                .map(t -> ((Token<Token>) t).getValue().getValue().toString()).collect(Collectors.toList());
        Set<String> duplicates = findDuplicates(referencesJump);
        if (!duplicates.isEmpty()) {
            String duplicatesS = String.join(", ", duplicates);
            Logger.warning("Multiple Jump Definitions: \"" + duplicatesS + '\"');
        }
        List<String> referencesVar = query
                .whereEqual(Token::getType, TokenType.CONSTANT)
                .or()
                .whereEqual(Token::getType, TokenType.DEFINITION).get()
                .stream()
                .map(t -> ((Token<Token>) t).getValue().getValue().toString()).collect(Collectors.toList());
        duplicates = findDuplicates(referencesVar);
        if (!duplicates.isEmpty()) {
            String duplicatesS = String.join(", ", duplicates);
            Logger.warning("Multiple Reference Definitions: \"" + duplicatesS + '\"');
        }
    }

    /**
     * Search for program statements that are not function calls
     *
     * @param token program token to check
     */
    private static void checkNonCalls(ProgramToken token) {
        List<Token> nonCalls = new ProgramQuery(token)
                .whereNotEqual(Token::getType, TokenType.DEFINITION)
                .and()
                .whereNotEqual(Token::getType, TokenType.CONSTANT)
                .and()
                .whereNotEqual(Token::getType, TokenType.CALL)
                .and()
                .whereNotEqual(Token::getType, TokenType.JUMP_POINT).get();
        for (Token t : nonCalls) {
            Logger.warning("not a function call: \"" + t.simpleName() + '\"');
        }
    }

    private static <T> Set<T> findDuplicates(Collection<T> collection) {
        Set<T> duplicates = new HashSet<>();
        Set<T> uniques = new HashSet<>();
        for (T t : collection) {
            if (!uniques.add(t)) {
                duplicates.add(t);
            }
        }
        return duplicates;
    }
}
