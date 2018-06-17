package edu.kit.mima.core.interpretation;

import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.parsing.token.ArrayToken;
import edu.kit.mima.core.parsing.token.BinaryToken;
import edu.kit.mima.core.parsing.token.ProgramToken;
import edu.kit.mima.core.parsing.token.Token;
import edu.kit.mima.core.parsing.token.TokenType;
import edu.kit.mima.core.parsing.token.Tuple;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Interprets the result of {@link Parser}
 *
 * @author Jannis Weis
 * @since 2018
 */
public class Interpreter {

    private final int wordLength;
    private int reservedIndex;
    private boolean running;

    //------------Jump----------------//
    private Environment jumpEnvironment;
    private int expressionScopeIndex;
    private boolean jumped;
    //--------------------------------//

    /**
     * @param wordLength number of bits in arguments
     */
    public Interpreter(final int wordLength) {
        this.wordLength = wordLength;
        reservedIndex = -1;
        expressionScopeIndex = 0;
        running = true;
        jumped = false;
    }

    /**
     * Evaluate the Program.
     *
     * @param program           program input created by {@link Parser}
     * @param globalEnvironment the global runtime environment
     * @return last evaluated result. If null then the last output had return-type void
     */
    public @Nullable Value<MachineWord> evaluateTopLevel(final ProgramToken program,
                                                         final Environment globalEnvironment) {
        Environment runtimeEnvironment = globalEnvironment;
        Value<MachineWord> value = null;
        ProgramToken runtimeToken = program;
        boolean firstScope = true;
        while (running) {
            /*
             * First call has to create own scope that releases memory.
             * As calls/jumps can only go up in scopes the scope doesn't need to be renewed, and
             * memory doesn't need to be released. Clearing memory will be done by the first
             * environment call.
             */
            value = evaluateProgram(runtimeToken, runtimeEnvironment, firstScope, !firstScope, expressionScopeIndex);
            if (firstScope) {
                firstScope = false;
            }
            if (jumped) {
                runtimeEnvironment = jumpEnvironment;
                runtimeToken = jumpEnvironment.getProgramToken();
                jumped = false;
            }
        }
        return value;
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
     * Set the current running status of the interpreter.
     * If false no statements will be evaluated. Is not a pause method, stopping
     * the interpreter yields in returning out of the evaluateTopLevel() method.
     *
     * @param running running status
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    /*
     * Create jump associations for the given environment based on the tokens
     * This needs to be done as forward referencing is allowed for jumps
     */
    @SuppressWarnings("unchecked")
    private void resolveJumpPoints(final Token[] tokens, final Environment environment) {
        for (int i = 0; i < tokens.length; i++) {
            Token token = tokens[i];
            if (token.getType() == TokenType.JUMP_POINT) {
                environment.defineJump(((Tuple<Token, Token>) token).getFirst(), i);
            }
        }
    }

    /*
     * Evaluate different tokens
     */
    @SuppressWarnings("unchecked")
    private @Nullable Value<MachineWord> evaluate(final Token expression, final Environment environment) {
        if (!running) {
            return null;
        }
        switch (expression.getType()) {
            case PROGRAM:
                /*
                 * Subprograms need to have own scope for variable shadowing.
                 * They should also release their memory and start at index 0
                 */
                return evaluateProgram((ProgramToken) expression, environment, true, true, 0);
            case NUMBER:
                return evaluateNumber((String) expression.getValue());
            case EMPTY:
                return new Value<>(ValueType.VOID, (MachineWord) expression.getValue());
            case BINARY:
                return evaluateBinary((String) expression.getValue());
            case IDENTIFICATION:
                return evaluateIdentification(expression, environment);
            case DEFINITION:
                evaluateDefinition((BinaryToken<Token, Token>) expression, environment);
                return null;
            case CONSTANT:
                evaluateConstant((BinaryToken<Token, Token>) expression, environment);
                return null;
            case CALL:
                return evaluateFunction((BinaryToken<Token, ArrayToken<Token>>) expression, environment);
            case JUMP_POINT:
                return evaluate(((Tuple<Token, Token>) expression).getSecond(), environment);
            default:
                throw new IllegalArgumentException("Can't evaluate: " + expression);
        }
    }

    /**
     * Evaluate a program Token
     *
     * @param programToken  the program token to be evaluated
     * @param environment   run environment
     * @param ownScope      whether the program should be contained in an own scope.
     * @param releaseMemory whether the memory addresses should be released after execution
     * @param scopeIndex    startIndex in scope
     * @return last evaluated statement
     */
    private Value<MachineWord> evaluateProgram(final ProgramToken programToken, final Environment environment,
                                               boolean ownScope, boolean releaseMemory, int scopeIndex) {
        Environment scope = environment;
        Token[] tokens = programToken.getValue();
        if (ownScope) {
            scope = environment.extend(programToken); //Extend to own scope
            resolveJumpPoints(tokens, scope);
        }
        scope.setExpressionIndex(scopeIndex);
        int reserved = reservedIndex; //Remember index for auto created memory cells

        Value<MachineWord> value = null;
        //Todo intercept here for stepwise execution using threads
        while (!jumped && running && scope.getExpressionIndex() < tokens.length) {
            value = evaluate(tokens[scope.getExpressionIndex()], scope);
            scope.setExpressionIndex(scope.getExpressionIndex() + 1);
        }

        if (!jumped) {
            expressionScopeIndex = 0;
        }
        if (releaseMemory) {
            reservedIndex = reserved; //Release auto created memory cells
        }
        return jumped ? null : value; //Automatically return to parent scope
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
     * Evaluate a number string
     */
    private Value<MachineWord> evaluateNumber(String value) {
        try {
            return new Value<>(ValueType.NUMBER, new MachineWord(Integer.parseInt(value), wordLength));
        } catch (NumberFormatException e) {
            throw new NumberFormatException(e.getMessage());
        }
    }

    /*
     * Evaluate a number value
     */
    private Value<MachineWord> evaluateNumber(int value) {
        return new Value<>(ValueType.NUMBER, new MachineWord(value, wordLength));
    }

    /*
     * Evaluate a Identification reference
     */
    private Value<MachineWord> evaluateIdentification(final Token token, final Environment environment) {
        MachineWord value;
        ValueType type;
        try {
            value = environment.getVariable(token);
            type = ValueType.MEMORY_REFERENCE;
        } catch (IllegalArgumentException e1) {
            try {
                value = environment.getConstant(token);
                type = ValueType.CONSTANT;
            } catch (IllegalArgumentException e2) {
                value = new MachineWord(environment.getJump(token), wordLength);
                type = ValueType.JUMP_REFERENCE;
                prepareJump(environment.lookupJump(token));
            }
        }
        return new Value<>(type, value);
    }

    /*
     + Evaluate constant definitions
     */
    private void evaluateConstant(BinaryToken<Token, Token> definition, Environment environment) {
        var expressionValue = evaluate(definition.getSecond(), environment);
        if (expressionValue == null) {
            throw new IllegalArgumentException("Not a definition body: " + definition.getSecond());
        }
        environment.defineConstant(definition.getFirst(), expressionValue.getValue());
    }

    /*
     * Evaluate memory reference definitions
     */
    private void evaluateDefinition(BinaryToken<Token, Token> definition, Environment environment) {
        if (definition.getSecond().getType() == TokenType.EMPTY) {
            environment.defineVariable(definition.getFirst(), evaluateNumber(reservedIndex).getValue());
            reservedIndex--;
        } else {
            var expressionValue = evaluate(definition.getSecond(), environment);
            if (expressionValue == null) {
                throw new IllegalArgumentException("Not a definition body: " + definition.getSecond());
            }
            if (expressionValue.getValue().intValue() < 0) {
                throw new IllegalArgumentException("Can't have negative memory references");
            }
            environment.defineVariable(definition.getFirst(), expressionValue.getValue());
        }
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
}
