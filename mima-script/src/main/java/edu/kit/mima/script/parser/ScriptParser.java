package edu.kit.mima.script.parser;

import edu.kit.mima.core.parsing.Processor;
import edu.kit.mima.core.parsing.inputstream.CharInputStream;
import edu.kit.mima.core.parsing.inputstream.TokenStream;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.token.AtomToken;
import edu.kit.mima.core.token.BinaryToken;
import edu.kit.mima.core.token.EmptyToken;
import edu.kit.mima.core.token.ListToken;
import edu.kit.mima.core.token.ProgramToken;
import edu.kit.mima.core.token.Token;
import edu.kit.mima.core.token.TokenType;
import edu.kit.mima.script.lang.Operation;
import edu.kit.mima.script.lang.ScriptKeyword;
import edu.kit.mima.script.token.BooleanToken;
import edu.kit.mima.script.token.ConditionalToken;
import edu.kit.mima.script.token.FunctionToken;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class ScriptParser extends Processor<Token<?>, ScriptTokenStream> {


    public ScriptParser(@NotNull final ScriptTokenStream input) {
        super(input);
    }

    public ProgramToken parseTopLevel() {
        final List<Token<?>> program = new ArrayList<>();
        while (!input.isEmpty()) {
            program.add(maybeList(this::parseExpression));
            if (!input.isEmpty() && program.get(program.size() - 1).getType() != TokenType.CONDITIONAL) {
                skipPunctuation(Punctuation.INSTRUCTION_END);
            }
        }
        return new ProgramToken(program.toArray(new Token<?>[0]), null,0,0, 0);
    }

    private Token<?> maybeList(final Supplier<Token<?>> supplier) {
        var token = supplier.get();
        if (isPunctuation(Punctuation.COMMA)) {
            input.next();
            var list = new ArrayList<Token<?>>();
            list.add(token);
            list.addAll(delimited(new char[]{CharInputStream.EMPTY_CHAR,
                                          Punctuation.INSTRUCTION_END,
                                          Punctuation.COMMA},
                                  this::parseExpression, false).getValue());
            return new ListToken<>(list);
        }
        return token;
    }

    private Token<?> parseExpression() {
        return maybeCall(() -> maybeBinary(parseAtomic(), 0));
    }

    private ProgramToken parseProgram() {
        var line = input.getLine();
        var prog = delimited(new char[]{Punctuation.SCOPE_OPEN,
                                     Punctuation.SCOPE_CLOSED,
                                     Punctuation.INSTRUCTION_END},
                             this::parseExpression, true);
//        if (prog.getValue().size() == 0) {
//            return BooleanToken.createTrue(0, line);
//        } else if (prog.getValue().size() == 1) {
//            return prog.getValue().get(0);
//        } else {
        return new ProgramToken(prog.getValue().toArray(new Token<?>[0]),null, 0, 0, line);
//        }
    }

    private Token<?> parseIf() {
        skipKeyword(ScriptKeyword.IF);
        var cond = parseExpression();
        if (!isPunctuation(Punctuation.SCOPE_OPEN)) {
            skipKeyword(ScriptKeyword.THEN);
        }
        var then = parseExpression();
        if (isKeyword(ScriptKeyword.ELSE)) {
            input.next();
            return new ConditionalToken(cond, then, parseExpression());
        } else {
            return new ConditionalToken(cond, then);
        }
    }

    private Token<?> parseAtomic() {
        return maybeCall(() -> {
            if (isPunctuation(Punctuation.OPEN_BRACKET)) {
                input.next();
                var exp = parseExpression();
                skipPunctuation(Punctuation.CLOSED_BRACKET);
                return exp;
            }
            if (isPunctuation(Punctuation.SCOPE_OPEN)) {
                return parseProgram();
            }
            if (isKeyword(ScriptKeyword.IF)) {
                return parseIf();
            }
            if (isKeyword(ScriptKeyword.TRUE) || isKeyword(ScriptKeyword.FALSE)) {
                return parseBoolean();
            }
            if (isOperation()) {
                var op = input.next();
                var exp = parseExpression();
                return new BinaryToken<>(TokenType.UNARY, op, exp);
            }
            if (isKeyword(ScriptKeyword.FUNCTION)) {
                input.next();
                return parseFunction();
            }
            if (isKeyword(ScriptKeyword.RETURN)) {
                input.next();
                return parseReturn();
            }
            var token = input.next();
            if (token != null) {
                var type = token.getType();
                if (type == TokenType.IDENTIFICATION || type == TokenType.BINARY
                    || type == TokenType.NUMBER || type == TokenType.STRING) {
                    return token;
                }
            }
            return unexpected();
        });
    }


    private Token<?> parseFunction() {
        final var nameToken = input.next();
        if (nameToken == null || nameToken.getType() != TokenType.IDENTIFICATION) {
            return input.error("Expected identification");
        }
        final List<Token<?>> params = delimited(new char[]{Punctuation.OPEN_BRACKET,
                                                        Punctuation.CLOSED_BRACKET,
                                                        Punctuation.COMMA},
                                                this::parseVariable, true).getValue();
        final ProgramToken body = parseProgram();
        return new FunctionToken(nameToken.getValue().toString(), params, body);
    }

    private Token<?> parseVariable() {
        final var name = input.next();
        if (name == null || name.getType() != TokenType.IDENTIFICATION) {
            return input.error("Expecting variable name");
        }
        return name;
    }

    private Token<?> parseReturn() {
//        var values = delimited(new char[]{CharInputStream.EMPTY_CHAR,
//                                        Punctuation.INSTRUCTION_END,
//                                        Punctuation.COMMA},
//                               this::parseExpression, false);
        if (isPunctuation(Punctuation.INSTRUCTION_END)) {
            return new AtomToken<>(TokenType.RETURN, TokenStream.EMPTY);
        }
        var value = parseExpression();
        if (value == null) {
            return input.error("Expected token");
        }
        if (!TokenType.RETURN.<Set<TokenType>>getProperty("types").contains(value.getType())) {
            return input.error("Can't return " + value.getType());
        }
        return new AtomToken<>(TokenType.RETURN, value);
    }

    private Token<?> maybeCall(final Supplier<Token<?>> supplier) {
        var expr = supplier.get();
        if (isPunctuation(Punctuation.OPEN_BRACKET)) {
            return parseCall(expr);
        } else {
            return expr;
        }
    }

    private Token<?> parseCall(final Token<?> reference) {
        return new BinaryToken<>(
                TokenType.CALL,
                reference,
                delimited(new char[]{Punctuation.OPEN_BRACKET,
                                  Punctuation.CLOSED_BRACKET,
                                  Punctuation.COMMA},
                          this::parseExpression, true));
    }

    @SuppressWarnings("unchecked")
    private Token<?> maybeBinary(final Token<?> left, final int myPrecedence) {
        if (isOperation()) {
            final Token<Operation> operation = (Token<Operation>) input.peek();
            final int hisPrecedence = Operation.getPrecedenceForString(operation.getValue().toString());
            if (hisPrecedence > myPrecedence) {
                input.next();
                final Token<?> right = maybeBinary(parseAtomic(), hisPrecedence);
                final Token<?> binary = new BinaryToken<>(TokenType.BINARY_EXPR, operation,
                                                          new BinaryToken<>(TokenType.BINARY_EXPR, left, right));
                return maybeBinary(binary, myPrecedence);
            }
        }
        return left;
    }

    private Token<?> parseBoolean() {
        final int line = input.getLine();
        var token = input.next();
        if (token != null && Boolean.TRUE.toString().equals(token.getValue())) {
            return BooleanToken.createTrue(0, line);
        } else {
            return BooleanToken.createFalse(0, line);
        }
    }

    private boolean isOperation() {
        var token = input.peek();
        return token != null && token.getType() == TokenType.OPERATOR;
    }

    @Override
    protected Token<?> parseDelimiter() {
        return Optional.ofNullable(input.peek())
                       .map(t -> t.getType() == TokenType.PUNCTUATION)
                       .orElse(false)
               ? input.next()
               : new EmptyToken();
    }
}
