package edu.kit.mima.core.interpretation;

import edu.kit.mima.api.util.Tuple;
import edu.kit.mima.core.controller.DebugController;
import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.interpretation.environment.Environment;
import edu.kit.mima.core.interpretation.environment.GlobalEnvironment;
import edu.kit.mima.core.interpretation.stack.Continuation;
import edu.kit.mima.core.interpretation.stack.StackGuard;
import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.token.ArrayToken;
import edu.kit.mima.core.token.BinaryToken;
import edu.kit.mima.core.token.EmptyToken;
import edu.kit.mima.core.token.ProgramToken;
import edu.kit.mima.core.token.Token;
import edu.kit.mima.core.token.TokenType;
import edu.kit.mima.core.util.LambdaUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Interprets the result of {@link Parser}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class Interpreter {

    private static final Value<MachineWord> VOID = new Value<>(ValueType.VOID,
                                                               new MachineWord(0, 0));

    private final int wordLength;
    @NotNull private final StackGuard stackGuard;

    private final DebugController debugController;
    private final ExceptionHandler exceptionHandler;

    private boolean running;
    @Nullable private Token currentToken;
    private Environment currentScope;

    /**
     * Construct new Interpreter that uses the given number of bits in arguments.
     *
     * @param wordLength       number of bits in arguments
     * @param debugController  the debug controller
     * @param exceptionHandler the exception listener
     */
    public Interpreter(final int wordLength,
                       final DebugController debugController,
                       final ExceptionHandler exceptionHandler) {
        this.debugController = debugController;
        this.exceptionHandler = exceptionHandler;
        this.wordLength = wordLength;
        stackGuard = new StackGuard();
        running = false;
    }

    /**
     * Evaluate the Program.
     *
     * @param program           program input created by {@link Parser}
     * @param globalEnvironment the global runtime environment
     */
    public void evaluateTopLevel(@NotNull final ProgramToken program,
                                 @NotNull final Environment globalEnvironment) {
        running = true;
        final Environment runtimeEnvironment = globalEnvironment.extend(program);
        try {
            debugController.pause();
            execute(() -> {
                program.getJumps().forEach((t, i) -> runtimeEnvironment
                        .defineJump(t.getValue().toString(), i));
                evaluateProgram(program, runtimeEnvironment, v -> { });
            });
        } catch (@NotNull final IllegalArgumentException | IllegalStateException e) {
            exceptionHandler.notifyException(e);
        }
    }

    /**
     * Start and monitor program execution.
     *
     * @param continuation continuation to invoke execution with
     */
    private void execute(final Runnable continuation) {
        var func = continuation;
        while (running) {
            try {
                stackGuard.reset();
                func.run();
            } catch (@NotNull final Continuation cont) {
                func = cont.getContinuation();
            }
        }
    }

    /*
     * Evaluate different tokens
     */
    @SuppressWarnings("unchecked") /*Construction of tokens guarantees these types*/
    private void evaluate(@NotNull final Token expression, @NotNull final Environment environment,
                          @NotNull final Consumer<Value> callback) throws Continuation {
        stackGuard.guard(() -> evaluate(expression, environment, callback));
        switch (expression.getType()) {
            case PROGRAM:
                /*
                 * Subprograms need to have own scope for variable shadowing.
                 */
                final ProgramToken programToken = (ProgramToken) expression;
                final Environment scope = environment.extend(programToken);
                scope.setReservedIndex(environment.getReservedIndex());
                programToken.getJumps().forEach((t, i) -> scope
                        .defineJump(t.getValue().toString(), i));
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
                final var defToken = (ArrayToken<Token>) expression.getValue();
                evaluateDefinition(defToken, environment, callback);
                break;
            case CONSTANT:
                final var constToken = (ArrayToken<Token>) expression.getValue();
                evaluateConstant(constToken, environment, callback);
                break;
            case CALL:
                evaluateFunction((BinaryToken<Token, ArrayToken<Token>>) expression,
                                 environment, callback);
                break;
            case JUMP_POINT:
                evaluate(((Tuple<Token, Token>) expression).getSecond(), environment, callback);
                break;
            default:
                fail("Can't evaluate: " + expression);
        }
    }

    public void jump(@NotNull final Environment toEnvironment,
                     final int instructionIndex,
                     @NotNull final Consumer<Value> callback) {
        toEnvironment.setExpressionIndex(instructionIndex);
        evaluateProgram(toEnvironment.getProgramToken(), toEnvironment, callback);
    }

    private void evaluateProgram(@NotNull final ProgramToken programToken,
                                 @NotNull final Environment environment,
                                 @NotNull final Consumer<Value> callback) throws Continuation {
        stackGuard.guard(() -> evaluateProgram(programToken, environment, callback));
        final Token[] tokens = programToken.getValue();
        currentScope = environment;
        final int startIndex = environment.getExpressionIndex();
        final BiConsumer<Value, Integer> loop = LambdaUtil.createRecursive(func -> (last, i) -> {
            if (!isRunning()) {
                return;
            }
            if (i < tokens.length) {
                currentToken = tokens[i];
            }
            if (i < tokens.length
                    && (i != startIndex || !(environment instanceof GlobalEnvironment))) {
                debugController.afterInstruction(tokens[i]);
            }
            if (i < tokens.length) {
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
    private void evaluateDefinition(@NotNull final ArrayToken<Token> token,
                                    @NotNull final Environment environment,
                                    @NotNull final Consumer<Value> callback) throws Continuation {
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
                        if (((MachineWord) v.getValue()).intValue() < 0) {
                            fail("Can't have negative memory references");
                        }
                        environment.defineVariable(
                                definition.getFirst().getValue().toString(),
                                (MachineWord) v.getValue()
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
    private void evaluateConstant(@NotNull final ArrayToken<Token> token,
                                  @NotNull final Environment environment,
                                  @NotNull final Consumer<Value> callback) throws Continuation {
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
                            (MachineWord) v.getValue()
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
    private void evaluateFunction(@NotNull final BinaryToken<Token, ArrayToken<Token>> value,
                                  @NotNull final Environment environment,
                                  final Consumer<Value> callback) throws Continuation {
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
    @NotNull
    @Contract("_ -> new")
    private Value<MachineWord> evaluateNumber(@NotNull final String value) {
        return new Value<>(ValueType.NUMBER, new MachineWord(Integer.parseInt(value), wordLength));
    }

    /*
     * Evaluate a binary string
     */
    @NotNull
    private Value<MachineWord> evaluateBinary(@NotNull final String binary) {
        final Boolean[] bits = new StringBuilder(binary).reverse().toString()
                .chars().mapToObj(i -> i == '1').toArray(Boolean[]::new);
        return new Value<>(ValueType.NUMBER, new MachineWord(bits, wordLength));
    }

    /*
     * Evaluate a Identification reference
     */
    @Nullable
    private Value evaluateIdentification(@NotNull final Token token,
                                         @NotNull final Environment environment) {
        final MachineWord value;
        final ValueType type;
        final String name = token.getValue().toString();
        if (environment.lookupVariable(name) != Environment.EMPTY_ENV) {
            value = environment.getVariable(name);
            type = ValueType.MEMORY_REFERENCE;
        } else if (environment.lookupConstant(name) != Environment.EMPTY_ENV) {
            value = environment.getConstant(name);
            type = ValueType.CONSTANT;
        } else if (environment.lookupJump(name) != Environment.EMPTY_ENV) {
            return new Value<>(ValueType.JUMP_REFERENCE, name);
        } else {
            return fail("Undefined Identification: " + token.getValue());
        }
        return new Value<>(type, value);
    }

    @Nullable
    private Value<MachineWord> fail(final String message) {
        exceptionHandler.notifyException(new InterpreterException(message));
        debugController.stop();
        running = false;
        return VOID;
    }

    /**
     * Returns whether the interpreter is running.
     *
     * @return true if running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Set the current running status of the interpreter. If false no statements will be evaluated.
     * Is not a pause method, stopping the interpreter yields in returning out of the
     * evaluateTopLevel() method.
     *
     * @param running running status
     */
    public void setRunning(final boolean running) {
        this.running = running;
    }

    /**
     * Return the current token.
     *
     * @return current Token
     */
    @Nullable
    public Token getCurrentToken() {
        return currentToken;
    }

    /**
     * Get the current evaluation scope.
     *
     * @return Environment object of current scope
     */
    public Environment getCurrentScope() {
        return currentScope;
    }
}