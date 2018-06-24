package edu.kit.mima.core.interpretation;

import edu.kit.mima.core.controller.DebugController;
import edu.kit.mima.core.controller.ReferenceCrawler;
import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.parsing.token.ArrayToken;
import edu.kit.mima.core.parsing.token.BinaryToken;
import edu.kit.mima.core.parsing.token.ProgramToken;
import edu.kit.mima.core.parsing.token.Token;
import edu.kit.mima.core.parsing.token.TokenType;
import edu.kit.mima.core.parsing.token.Tuple;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Interprets the result of {@link Parser}
 *
 * @author Jannis Weis
 * @since 2018
 */
public class Interpreter {

    private static final Value<MachineWord> VOID = new Value<>(ValueType.VOID, null);

    private final int wordLength;
    private int reservedIndex;
    private boolean running;

    //------------Debug---------------//
    private final DebugController debugController;
    private ExceptionListener exceptionListener;
    private boolean debug;
    private Token currentToken;
    private Environment currentScope;
    //--------------------------------//

    //------------Jump----------------//
    private Environment jumpEnvironment;
    private int expressionScopeIndex;
    private boolean jumped;
    //--------------------------------//


    /**
     * Construct new Interpreter that uses the given number of bits in arguments
     *
     * @param wordLength        number of bits in arguments
     * @param debugController   the debug controller
     * @param exceptionListener the exception listener
     */
    public Interpreter(final int wordLength, DebugController debugController, ExceptionListener exceptionListener) {
        this.debugController = debugController;
        this.exceptionListener = exceptionListener;
        this.wordLength = wordLength;
        reservedIndex = -1;
        expressionScopeIndex = 0;
        jumped = false;
    }

    /*
     * Create jump associations for the given environment based on the tokens
     * This needs to be done as forward referencing is allowed for jumps
     */
    @SuppressWarnings("unchecked")
    private void resolveJumpPoints(final ProgramToken programToken, final Environment environment) {
        try {
            for (var pair : new ReferenceCrawler(programToken).getJumpPoints()) {
                environment.defineJump(pair.getKey(), pair.getValue());
            }
        } catch (IllegalArgumentException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Evaluate the Program.
     *
     * @param program           program input created by {@link Parser}
     * @param globalEnvironment the global runtime environment
     * @param debug             whether the interpreter should pause after each statement
     */
    public void evaluateTopLevel(final ProgramToken program, final Environment globalEnvironment,
                                 boolean debug) {
        /*Initializer*/
        this.debug = debug;
        running = true;
        jumped = false;
        reservedIndex = -1;
        expressionScopeIndex = 0;

        Environment runtimeEnvironment = globalEnvironment.extend(program);
        ProgramToken runtimeToken = program;
        resolveJumpPoints(program, runtimeEnvironment);
        boolean firstScope = true;
        while (running) {
            /*
             * First call has to create own scope that releases memory.
             * As calls/jumps can only go up in scopes the scope doesn't need to be renewed, and
             * memory doesn't need to be released. Clearing memory will be done by the first
             * environment call.
             */
            evaluateProgram(runtimeToken, runtimeEnvironment, firstScope, expressionScopeIndex);
            if (firstScope) {
                firstScope = false;
            }
            if (jumped) {
                runtimeEnvironment = jumpEnvironment;
                runtimeToken = jumpEnvironment.getProgramToken();
                jumped = false;
            }
        }
        running = false;
    }

    /*
     * Evaluate different tokens
     */
    @SuppressWarnings("unchecked")
    private Value<MachineWord> evaluate(final Token expression, final Environment environment) {
        if (!running) {
            return VOID;
        }
        switch (expression.getType()) {
            case PROGRAM:
                /*
                 * Subprograms need to have own scope for variable shadowing.
                 * They should also release their memory and start at index 0
                 */
                ProgramToken programToken = (ProgramToken) expression;
                Environment scope = environment.extend(programToken);
                resolveJumpPoints(programToken, scope);
                return evaluateProgram(programToken, environment, true, 0);
            case NUMBER:
                return evaluateNumber((String) expression.getValue());
            case EMPTY:
                return VOID;
            case BINARY:
                return evaluateBinary((String) expression.getValue());
            case IDENTIFICATION:
                return evaluateIdentification(expression, environment);
            case DEFINITION:
                Token[] definitionTokens = (Token[])((ArrayToken)expression.getValue()).getValue();
                for (Token token : definitionTokens) {
                    evaluateDefinition((BinaryToken<Token, Token>)token, environment);
                }
                return VOID;
            case CONSTANT:
                Token[] constantTokens = (Token[])((ArrayToken)expression.getValue()).getValue();
                for (Token token : constantTokens) {
                    evaluateConstant((BinaryToken<Token, Token>)token, environment);
                }
                return VOID;
            case CALL:
                return evaluateFunction((BinaryToken<Token, ArrayToken<Token>>) expression, environment);
            case JUMP_POINT:
                return evaluate(((Tuple<Token, Token>) expression).getSecond(), environment);
            default:
                return fail("Can't evaluate: " + expression);
        }
    }

    /**
     * Evaluate a program Token
     *
     * @param programToken  the program token to be evaluated
     * @param scope         run environment
     * @param releaseMemory whether the memory addresses should be released after execution
     * @param scopeIndex    startIndex in scope
     * @return last evaluated statement
     */
    private Value<MachineWord> evaluateProgram(final ProgramToken programToken, final Environment scope,
                                               boolean releaseMemory, int scopeIndex) {
        Token[] tokens = programToken.getValue();
        scope.setExpressionIndex(scopeIndex);
        int reserved = reservedIndex; //Remember index for auto created memory cells

        Value<MachineWord> value = VOID;
        while (!jumped && running && scope.getExpressionIndex() < tokens.length) {
            currentToken = tokens[scope.getExpressionIndex()];
            currentScope = scope;
            value = evaluate(currentToken, scope);
            scope.setExpressionIndex(scope.getExpressionIndex() + 1);
            if (debug) {
                debugController.pause();
            }
        }

        if (!jumped) {
            expressionScopeIndex = 0;
        }
        if (releaseMemory) {
            reservedIndex = reserved; //Release auto created memory cells
        }
        return jumped ? VOID : value; //Automatically return to parent scope
    }

    /*
     * Evaluate a number string
     */
    private Value<MachineWord> evaluateNumber(String value) {
        return new Value<>(ValueType.NUMBER, new MachineWord(Integer.parseInt(value), wordLength));
    }

    /*
     * Evaluate a binary string
     */
    private Value<MachineWord> evaluateBinary(String binary) {
        Boolean[] bits = new StringBuilder(binary).reverse().toString()
                .chars().mapToObj(i -> i == '1').toArray(Boolean[]::new);
        return new Value<>(ValueType.NUMBER, new MachineWord(bits, wordLength));
    }

    /*
     * Evaluate a Identification reference
     */
    private Value<MachineWord> evaluateIdentification(final Token token, final Environment environment) {
        MachineWord value;
        ValueType type;
        if (environment.lookupVariable(token) != null) {
            value = environment.getVariable(token);
            type = ValueType.MEMORY_REFERENCE;
        } else if (environment.lookupConstant(token) != null) {
            value = environment.getConstant(token);
            type = ValueType.CONSTANT;
        } else if (environment.lookupJump(token) != null) {
            value = new MachineWord(environment.getJump(token), wordLength);
            type = ValueType.JUMP_REFERENCE;
            prepareJump(environment.lookupJump(token));
        } else {
            return fail("Undefined Identification: " + token.getValue());
        }
        return new Value<>(type, value);
    }

    /*
     * Evaluate memory reference definitions
     */
    private void evaluateDefinition(BinaryToken<Token, Token> definition, Environment environment) {
        if (definition.getSecond().getType() == TokenType.EMPTY) {
            environment.defineVariable(definition.getFirst(), evaluateNumber(String.valueOf(reservedIndex)).getValue());
            reservedIndex--;
        } else {
            var expressionValue = checkDefinitionBody(definition.getSecond(), environment);
            if (expressionValue.getValue().intValue() < 0) {
                fail("Can't have negative memory references");
            }
            environment.defineVariable(definition.getFirst(), expressionValue.getValue());
        }
    }

    /*
     * Evaluate constant definitions
     */
    private void evaluateConstant(BinaryToken<Token, Token> definition, Environment environment) {
        var expressionValue = checkDefinitionBody(definition.getSecond(), environment);
        environment.defineConstant(definition.getFirst(), expressionValue.getValue());
    }

    /*
     * check if given expressions is a definition body
     */
    private Value<MachineWord> checkDefinitionBody(Token body, Environment environment) {
        var expressionValue = evaluate(body, environment);
        if (Objects.equals(expressionValue, VOID)) {
            fail("Not a definition body: " + body);
        }
        return expressionValue;
    }

    /*
     * Evaluate a function call
     */
    @SuppressWarnings("unchecked")
    private Value<MachineWord> evaluateFunction(BinaryToken<Token, ArrayToken<Token>> value, Environment environment) {
        var function = environment.getFunction(value.getFirst());
        Token[] arguments = value.getSecond().getValue();
        List<Value<MachineWord>> args = Arrays.stream(arguments)
                .map(argument -> evaluate(argument, environment)).collect(Collectors.toList());
        return new Value<>(ValueType.NUMBER, function.apply(args, environment));
    }

    private Value<MachineWord> fail(String message) {
        exceptionListener.notifyException(new InterpreterException(message));
        debugController.stop();
        running = false;
        return VOID;
    }

    /**
     * Set the environment the next jump falls in
     *
     * @param jumpEnvironment destination Environment
     */
    public void prepareJump(Environment jumpEnvironment) {
        this.jumpEnvironment = jumpEnvironment;
    }

    /**
     * Performs the jump to the given index
     *
     * @param instructionIndex index in jump environment
     */
    public void performJump(int instructionIndex) {
        jumped = true;
        expressionScopeIndex = instructionIndex;
    }

    /**
     * Returns whether the interpreter is running
     *
     * @return true if running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Set the current running status of the interpreter.
     * If false no statements will be evaluated. Is not a pause method, stopping
     * the interpreter yields in returning out of the evaluateTopLevel() method.
     *
     * @param running running status
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Return the current token
     *
     * @return current Token
     */
    public Token getCurrentToken() {
        return currentToken;
    }

    /**
     * Get the current evaluation scope
     *
     * @return Environment object of current scope
     */
    public Environment getCurrentScope() {
        return currentScope;
    }
}
