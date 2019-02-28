package edu.kit.mima.core.interpretation;

import edu.kit.mima.core.controller.DebugController;
import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.interpretation.environment.Environment;
import edu.kit.mima.core.interpretation.stack.Continuation;
import edu.kit.mima.core.interpretation.stack.StackGuard;
import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.parsing.token.ArrayToken;
import edu.kit.mima.core.parsing.token.BinaryToken;
import edu.kit.mima.core.parsing.token.EmptyToken;
import edu.kit.mima.core.parsing.token.ProgramToken;
import edu.kit.mima.core.parsing.token.Token;
import edu.kit.mima.core.parsing.token.TokenType;
import edu.kit.mima.core.parsing.token.Tuple;
import edu.kit.mima.core.query.programQuery.ProgramQuery;
import edu.kit.mima.core.query.programQuery.ProgramQueryResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Interprets the result of {@link Parser}
 *
 * @author Jannis Weis
 * @since 2018
 */
public class Interpreter {

    private static final Value<MachineWord> VOID = new Value<>(ValueType.VOID, null);

    private final int wordLength;
    private final StackGuard stackGuard;

    private final DebugController debugController;
    private final ExceptionListener exceptionListener;

    private boolean running;
    private Token currentToken;
    private Environment currentScope;


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
        stackGuard = new StackGuard();
        running = false;
    }

    /*
     * Create jump associations for the given environment based on the tokens
     * This needs to be done as forward referencing is allowed for jumps
     */
    private void resolveJumpPoints(final ProgramToken programToken, final Environment environment) {
        try {
            List<Token> tokens = ((ProgramQueryResult)new ProgramQuery(programToken)
                    .whereEqual(Token::getType, TokenType.JUMP_POINT)).get(false);
            for (var token : tokens) {
                environment.defineJump(((Token)token.getValue()).getValue().toString(), token.getIndex());
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
     */
    public void evaluateTopLevel(final ProgramToken program, final Environment globalEnvironment) {
        running = true;
        Environment runtimeEnvironment = globalEnvironment.extend(program);
        try {
            execute(() -> {
                resolveJumpPoints(program, runtimeEnvironment);
                evaluateProgram(program, runtimeEnvironment, v -> {/*Stop program*/});
            });
        } catch (IllegalArgumentException | IllegalStateException e) {
            exceptionListener.notifyException(e);
        }
    }

    /**
     * Start and monitor program execution
     *
     * @param continuation continuation to invoke execution with
     */
    private void execute(Runnable continuation) {
        var func = continuation;
        while(running) {
            try {
                stackGuard.reset();
                func.run();
            } catch (Continuation cont) {
                func = cont.getContinuation();
            }
        }
    }

    /*
     * Evaluate different tokens
     */
    @SuppressWarnings("unchecked") /*Construction of tokens guarantees these types*/
    private void evaluate(final Token expression, final Environment environment,
                                        final Consumer<Value> callback) throws Continuation  {
        stackGuard.guard(() -> evaluate(expression, environment, callback));
        switch (expression.getType()) {
            case PROGRAM:
                /*
                 * Subprograms need to have own scope for variable shadowing.
                 */
                ProgramToken programToken = (ProgramToken) expression;
                Environment scope = environment.extend(programToken);
                scope.setReservedIndex(environment.getReservedIndex());
                resolveJumpPoints(programToken, scope);
                evaluateProgram(programToken, scope, callback);
                break;
            case NUMBER:
                callback.accept(evaluateNumber((String) expression.getValue()));
                break;
            case EMPTY:
                callback.accept(VOID);
                break;
            case BINARY:
                callback.accept(evaluateBinary((String) expression.getValue()));
                break;
            case IDENTIFICATION:
                callback.accept(evaluateIdentification(expression, environment));
                break;
            case DEFINITION:
                var defToken = (ArrayToken<Token>) expression.getValue();
                evaluateDefinition(defToken, environment, callback);
                break;
            case CONSTANT:
                var constToken = (ArrayToken<Token>) expression.getValue();
                evaluateConstant(constToken, environment, callback);
                break;
            case CALL:
                evaluateFunction((BinaryToken<Token, ArrayToken<Token>>) expression, environment, callback);
                break;
            case JUMP_POINT:
                evaluate(((Tuple<Token, Token>) expression).getSecond(), environment, callback);
                break;
            default:
                fail("Can't evaluate: " + expression);
        }
    }

    public void jump(Environment toEnvironment, int instructionIndex, Consumer<Value> callback) {
        toEnvironment.setExpressionIndex(instructionIndex);
        debugController.pause();
        evaluateProgram(toEnvironment.getProgramToken(), toEnvironment, callback);
    }

    private void evaluateProgram(ProgramToken programToken, Environment environment,
                                 Consumer<Value> callback) throws Continuation  {
        stackGuard.guard(() -> evaluateProgram(programToken, environment, callback));
        Token[] tokens = programToken.getValue();
        currentScope = environment;
        int startIndex = environment.getExpressionIndex();
        BiConsumer<Value, Integer> loop = LambdaUtil.createRecursive(func -> (last, i) -> {
           if (running && i != startIndex) {
               debugController.pause();
           }
           if (i < tokens.length && running) {
               environment.setExpressionIndex(i);
               currentToken = tokens[i];
               evaluate(currentToken, environment, v -> func.accept(v, i + 1));
           } else {
               environment.setExpressionIndex(0);
               callback.accept(last);
           }
        });
        loop.accept(null, startIndex);
    }

    @SuppressWarnings("unchecked") /*Construction of tokens guarantees these types*/
    private void evaluateDefinition(ArrayToken<Token> token, Environment environment,
                                    Consumer<Value> callback) throws Continuation  {
        stackGuard.guard(() -> evaluateDefinition(token, environment, callback));
        Token[] tokens = token.getValue();
        BiConsumer<Environment, Integer> loop = LambdaUtil.createRecursive(func -> (env, i) -> {
            if (i < tokens.length) {
                BinaryToken<Token, Token> definition = ((BinaryToken<Token, Token>) tokens[i]);
                if (definition.getSecond().getType() == TokenType.EMPTY) {
                    environment.defineVariable(
                            definition.getFirst().getValue().toString(),
                            evaluateNumber(String.valueOf(env.getReservedIndex())).getValue()
                    );
                    env.setReservedIndex(env.getReservedIndex() - 1);
                    func.accept(environment, i + 1);
                } else {
                    evaluate(definition.getSecond(), environment, v -> {
                        if (Objects.equals(v, VOID)) {
                            fail("Not a definition body: " + definition.getSecond());
                        }
                        if (((MachineWord)v.getValue()).intValue() < 0) {
                            fail("Can't have negative memory references");
                        }
                        environment.defineVariable(
                                definition.getFirst().getValue().toString(),
                                (MachineWord)v.getValue()
                        );
                        func.accept(environment, i + 1);
                    });
                }
            } else {
                evaluate(new EmptyToken(), environment, callback);
            }
        });
        loop.accept(environment, 0);
    }

    @SuppressWarnings("unchecked") /*Construction of tokens guarantees these types*/
    private void evaluateConstant(ArrayToken<Token> token, Environment environment,
                                  Consumer<Value> callback) throws Continuation  {
        stackGuard.guard(() -> evaluateConstant(token, environment, callback));
        Token[] tokens = token.getValue();
        BiConsumer<Environment, Integer> loop = LambdaUtil.createRecursive(func -> (env, i) -> {
            if (i < tokens.length) {
                BinaryToken<Token, Token> definition = ((BinaryToken<Token, Token>) tokens[i]);
                evaluate(definition.getSecond(), environment, v -> {
                    if (Objects.equals(v, VOID)) {
                        fail("Not a definition body: " + definition.getSecond());
                    }
                    environment.defineConstant(
                            definition.getFirst().getValue().toString(),
                            (MachineWord)v.getValue()
                    );
                    func.accept(environment, i + 1);
                });
            } else {
                evaluate(new EmptyToken(), environment, callback);
            }
        });
        loop.accept(environment, 0);
    }

    /*
     * Evaluate a function call
     */
    private void evaluateFunction(BinaryToken<Token, ArrayToken<Token>> value, Environment environment,
                                  Consumer<Value> callback) throws Continuation {
        stackGuard.guard(() -> evaluateFunction(value, environment, callback));
        Token[] arguments = value.getSecond().getValue();
        BiConsumer<List<Value>, Integer> loop = LambdaUtil.createRecursive(func -> (args, i) -> {
            if (i < arguments.length) {
                evaluate(arguments[i], environment, v2 -> {
                    args.add(v2);
                    func.accept(args, i + 1);
                });
            } else {
                var function = environment.getFunction(value.getFirst().getValue().toString());
                function.apply(args, environment, callback);
            }
        });
        loop.accept(new ArrayList<>(), 0);
    }

    /*
     * Evaluate a number string
     */
    private Value<MachineWord> evaluateNumber(final String value) {
        return new Value<>(ValueType.NUMBER, new MachineWord(Integer.parseInt(value), wordLength));
    }

    /*
     * Evaluate a binary string
     */
    private Value<MachineWord> evaluateBinary(final String binary) {
        Boolean[] bits = new StringBuilder(binary).reverse().toString()
                .chars().mapToObj(i -> i == '1').toArray(Boolean[]::new);
        return new Value<>(ValueType.NUMBER, new MachineWord(bits, wordLength));
    }

    /*
     * Evaluate a Identification reference
     */
    private Value evaluateIdentification(final Token token, final Environment environment) {
        MachineWord value;
        ValueType type;
        String name = token.getValue().toString();
        if (environment.lookupVariable(name) != null) {
            value = environment.getVariable(name);
            type = ValueType.MEMORY_REFERENCE;
        } else if (environment.lookupConstant(name) != null) {
            value = environment.getConstant(name);
            type = ValueType.CONSTANT;
        } else if (environment.lookupJump(name) != null) {
            return new Value<>(ValueType.JUMP_REFERENCE, name);
        } else {
            return fail("Undefined Identification: " + token.getValue());
        }
        return new Value<>(type, value);
    }

    private Value<MachineWord> fail(String message) {
        exceptionListener.notifyException(new InterpreterException(message));
        debugController.stop();
        running = false;
        return VOID;
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
