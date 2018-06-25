package edu.kit.mima.core.controller;

import edu.kit.mima.core.parsing.token.ProgramToken;
import edu.kit.mima.core.parsing.token.Token;
import edu.kit.mima.gui.logging.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        var sets = new ReferenceCrawler(token).getReferences();
        Set<String> references = new HashSet<>();
        for (var string : sets.get(1)) {
            if (!references.add(string)) {
                Logger.warning("jump reference is defined multiple times: \"" + string + '\"');
            }
        }
        references.clear();
        for (var set : Set.of(sets.get(0), sets.get(2))) {
            for (var string : set) {
                if (!references.add(string)) {
                    Logger.warning("reference is defined multiple times: \"" + string + '\"');
                }
            }
        }
    }

    /**
     * Search for program statements that are not function calls
     *
     * @param token program token to check
     */
    private static void checkNonCalls(ProgramToken token) {
        List<Token> nonCalls = new ReferenceCrawler(token).getNonFunctions();
        for (Token t : nonCalls) {
            Logger.warning("not a function call: \"" + t.simpleName() + '\"');
        }
    }
}
