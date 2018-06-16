package edu.kit.mima.core.interpretation;

import edu.kit.mima.core.Mima;
import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.instruction.MimaInstruction;
import edu.kit.mima.core.instruction.MimaXInstruction;
import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.parsing.token.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Interprets the result of {@link Parser}
 *
 * @author Jannis Weis
 * @since 2018
 */
public class Interpreter {

    private final ProgramToken program;

    private final int wordLength;
    private int reservedIndex;
    private Environment lastReferencedEnvironment;
    private boolean jumped;
    private boolean running;

    /**
     * @param program    program input
     * @param wordLength number of bits in MachineWord
     */
    public Interpreter(final ProgramToken program, final int wordLength) {
        this.program = program;
        this.wordLength = wordLength;
        reservedIndex = -1;
        running = true;
        jumped = false;
    }

    /**
     * Evaluate the Program.
     *
     * @return last evaluated result. If null then the last output had return-type void
     */
    public @Nullable Value<MachineWord> evaluate() {
        Environment environment = setupGlobalEnvironment();
        Value<MachineWord> value = null;
        ProgramToken programToken = program;
        while (running) {
            value = evaluate(programToken, environment);
            if (jumped) {
                environment = lastReferencedEnvironment;
                programToken = lastReferencedEnvironment.getProgramToken();
                jumped = false;
            }
        }
        return value;
    }

    /*
     * Define the global instructions
     */
    private Environment setupGlobalEnvironment() {
        Environment globalEnv = new Environment(null, program);
        Mima mima = new Mima();
        MimaInstruction.setMima(mima);
        MimaXInstruction.setMima(mima);
        for (MimaInstruction instruction : MimaInstruction.values()) {
            globalEnv.defineFunction(new AtomToken<>(TokenType.IDENTIFICATION, instruction.toString()), instruction);
        }
        //Halt Instruction
        globalEnv.defineFunction(new AtomToken<>(TokenType.IDENTIFICATION, "HALT"), (args) -> {
            if (args.size() != 0) {
                throw new IllegalArgumentException("invalid number of arguments");
            }
            running = false;
            return null;
        });
        //Jump Instruction
        globalEnv.defineFunction(new AtomToken<>(TokenType.IDENTIFICATION, "JMP"), (args) -> {
            if (args.size() != 1) {
                throw new IllegalArgumentException("invalid number of arguments");
            }
            var argument = args.get(0);
            jumped = true;
            lastReferencedEnvironment.setExpressionIndex(argument.getValue().intValue());
            return null;
        });
        //Jump if negative Instruction
        globalEnv.defineFunction(new AtomToken<>(TokenType.IDENTIFICATION, "JMN"), (args) -> {
            if (args.size() != 1) {
                throw new IllegalArgumentException("invalid number of arguments");
            }
            if (mima.getAccumulator().msb() == 1) {
                var argument = args.get(0);
                jumped = true;
                lastReferencedEnvironment.setExpressionIndex(argument.getValue().intValue());
            }
            return null;
        });
        return globalEnv;
    }

    @SuppressWarnings("unchecked")
    private void resolveJumpPoints(Token[] tokens, Environment environment) {
        for (int i = 0; i < tokens.length; i++) {
            Token token = tokens[i];
            if (token.getType() == TokenType.JUMP_POINT) {
                resolveJumpPoint((Tuple<Token, Token>) token, environment, i);
            }
        }
    }

    private void resolveJumpPoint(Tuple<Token, Token> expression, Environment environment, int index) {
        environment.defineJump(expression.getFirst(), index);
        ((BinaryToken<Token, Token>) expression).setType(TokenType.RESOLVED_JUMP_POINT);
    }

    /**
     * @param expression  expression to evaluate
     * @param environment variable environment
     * @return MachineWord evaluation
     */
    @SuppressWarnings("unchecked")
    public @Nullable Value<MachineWord> evaluate(Token expression, Environment environment) {
        if (!running) {
            return null;
        }
        switch (expression.getType()) {
            case PROGRAM:
                return evaluateProgram((ProgramToken) expression, environment);
            case NUMBER:
                return evaluateNumber((String) expression.getValue());
            case EMPTY:
                return new Value<>(null, (MachineWord) expression.getValue());
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
            case RESOLVED_JUMP_POINT:
                return evaluate(((Tuple<Token, Token>) expression).getSecond(), environment);
            default:
                throw new IllegalArgumentException("Can't evaluate: " + expression);
        }
    }

    private Value<MachineWord> evaluateProgram(ProgramToken programToken, Environment environment) {
        Environment scope = environment.extend(programToken); //Extend to own scope
        Token[] tokens = programToken.getValue();
        resolveJumpPoints(tokens, scope);
        Value<MachineWord> value = null;
        while (environment.getExpressionIndex() < tokens.length) {
            value = evaluate(tokens[environment.getExpressionIndex()], scope);
            environment.setExpressionIndex(environment.getExpressionIndex() + 1);
            if (jumped) {
                return null;
            }
        }
        return value; //Automatically return to parent scope
    }

    private Value<MachineWord> evaluateBinary(String binary) {
        Boolean[] bits = new StringBuilder(binary).reverse().toString()
                .chars().mapToObj(i -> i == '1').toArray(Boolean[]::new);
        return new Value<>(ValueType.NUMBER, new MachineWord(bits, wordLength));
    }

    private Value<MachineWord> evaluateNumber(String value) {
        try {
            return new Value<>(ValueType.NUMBER, new MachineWord(Integer.parseInt(value), wordLength));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Not an integer: " + value);
        }
    }

    private Value<MachineWord> evaluateNumber(int value) {
        return new Value<>(ValueType.NUMBER, new MachineWord(value, wordLength));
    }

    private Value<MachineWord> evaluateIdentification(Token token, Environment environment) {
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
                lastReferencedEnvironment = environment.lookup(token);
            }
        }
        return new Value<>(type, value);
    }

    private void evaluateConstant(BinaryToken<Token, Token> definition, Environment environment) {
        var expressionValue = evaluate(definition.getSecond(), environment);
        if (expressionValue == null) {
            throw new IllegalArgumentException("Not a definition body: " + definition.getSecond());
        }
        environment.defineConstant(definition.getFirst(), expressionValue.getValue());
    }

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

    @SuppressWarnings("unchecked")
    private Value<MachineWord> evaluateFunction(BinaryToken<Token, ArrayToken<Token>> value, Environment environment) {
        var function = environment.getFunction(value.getFirst());
        Token[] arguments = value.getSecond().getValue();
        List<Value<MachineWord>> args = new ArrayList<>();
        for (Token argument : arguments) {
            args.add(evaluate(argument, environment));
        }
        return new Value<>(ValueType.NUMBER, function.apply(args));
    }
}
