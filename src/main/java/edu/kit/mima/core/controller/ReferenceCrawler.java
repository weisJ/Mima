package edu.kit.mima.core.controller;

import edu.kit.mima.core.parsing.token.ProgramToken;
import edu.kit.mima.core.parsing.token.Token;
import edu.kit.mima.core.parsing.token.TokenType;
import edu.kit.mima.core.parsing.token.Tuple;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jannis Weis
 * @since 2018
 */
public final class ReferenceCrawler {

    private ProgramToken programToken;

    /**
     * Create new Referecne Crawler object
     *
     * @param programToken the program token to search for references in
     */
    public ReferenceCrawler(ProgramToken programToken) {
        this.programToken = programToken;
    }

    /**
     * Search the parsed program for references and return as List of Sets
     * as follows: {{constant references}, {jump references}, {memory references}}
     *
     * @return list with references sets
     */
    public List<Set<String>> getReferences() {
        Set<String> memory = new HashSet<>();
        Set<String> constants = new HashSet<>();
        Set<String> jumps = new HashSet<>();
        Token[] tokens = programToken.getValue();
        for (Token token : tokens) {
            searchReferences(token, memory, constants, jumps);
        }
        return List.of(constants, jumps, memory);
    }

    private void searchReferences(Token token, Set<String> memory, Set<String> constants, Set<String> jump) {
        TokenType tokenType = token.getType();
        switch (tokenType) {
            case PROGRAM:
                Token[] tokens = ((ProgramToken) token).getValue();
                for (Token t : tokens) {
                    searchReferences(t, memory, constants, jump);
                }
                break;
            case JUMP_POINT:
                jump.add(((Token) token.getValue()).getValue().toString());
                break;
            case DEFINITION:
                memory.add(((Token) token.getValue()).getValue().toString());
                break;
            case CONSTANT:
                constants.add(((Token) token.getValue()).getValue().toString());
                break;
            default:
                break;
        }
    }

    /**
     * Create jump associations for the given environment based on the tokens
     * This needs to be done as forward referencing is allowed for jumps
     *
     * @return List of all jump points in order they appeared
     */
    public List<Pair<Token, Integer>> getJumpPoints() {
        List<Pair<Token, Integer>> references = new ArrayList<>();
        Token[] tokens = programToken.getValue();
        for (int i = 0; i < tokens.length; i++) {
            Token token = tokens[i];
            if (token.getType() == TokenType.JUMP_POINT) {
                references.add(new Pair<>(((Tuple<Token, Token>) token).getFirst(), i));
            }
        }
        return references;
    }

    /**
     * Returns all top level non function calls in the program
     *
     * @return List of all tokens that are not a jump reference or function call
     */
    public List<Token> getNonFunctions() {
        List<Token> found = new ArrayList<>();
        Token[] tokens = programToken.getValue();
        for (Token token : tokens) {
            searchNonFunctions(token, found);
        }
        return found;
    }

    private void searchNonFunctions(Token token, List<Token> found) {
        TokenType tokenType = token.getType();
        switch (tokenType) {
            case PROGRAM:
                Token[] tokens = ((ProgramToken) token).getValue();
                for (Token t : tokens) {
                    searchNonFunctions(t, found);
                }
                break;
            case KEYWORD:
            case PUNCTUATION:
            case BINARY:
            case NUMBER:
            case IDENTIFICATION:
            case ARRAY:
            case EMPTY:
            case ERROR:
                /* fall through */
                found.add(token);
                break;
            case CALL:
            case JUMP_POINT:
            case DEFINITION:
            case CONSTANT:
                /* fall through */
                break;
        }
    }
}
