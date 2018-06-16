package edu.kit.mima.core.interpretation;

import edu.kit.mima.core.Mima;
import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.instruction.MimaInstruction;
import edu.kit.mima.core.instruction.MimaXInstruction;
import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.parsing.token.ArrayToken;
import edu.kit.mima.core.parsing.token.AtomToken;
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

    private final ProgramToken program;

    private final int wordLength;
    private int reservedIndex;
    private int expressionScopeIndex;
    private boolean running;

    //Jump//
    private Environment lastReferencedEnvironment;
    private boolean jumped;
    //---//

    /**
     * @param program    program input
     * @param wordLength number of bits in MachineWord
     */
    public Interpreter(final ProgramToken program, final int wordLength) {
        this.program = program;
        this.wordLength = wordLength;
        reservedIndex = -1;
        expressionScopeIndex = 0;
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
                environment = lastReferencedEnvironment.returnToParent();
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
            if (!args.isEmpty()) {
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
            expressionScopeIndex = argument.getValue().intValue();
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
                expressionScopeIndex = argument.getValue().intValue();
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
                environment.defineJump(((Tuple<Token, Token>) token).getFirst(), i);
            }
        }
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
                return new Value<>(ValueType.EMPTY, (MachineWord) expression.getValue());
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

    private Value<MachineWord> evaluateProgram(ProgramToken programToken, Environment environment) {
        Environment scope = environment.extend(programToken); //Extend to own scope
        scope.setExpressionIndex(expressionScopeIndex);

        int reserved = reservedIndex; //Remember index for auto created memory cells

        Token[] tokens = programToken.getValue();
        resolveJumpPoints(tokens, scope);

        //Evaluate
        Value<MachineWord> value = null;
        while (scope.getExpressionIndex() < tokens.length) {
            value = evaluate(tokens[scope.getExpressionIndex()], scope);
            scope.setExpressionIndex(scope.getExpressionIndex() + 1);
            if (jumped) {
                return null;
            }
        }

        expressionScopeIndex = 0;
        reservedIndex = reserved; //Release auto created memory cells
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
            throw new NumberFormatException(e.getMessage());
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
                lastReferencedEnvironment = environment.lookupJump(token);
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
        List<Value<MachineWord>> args = Arrays.stream(arguments)
                .map(argument -> evaluate(argument, environment)).collect(Collectors.toList());
        return new Value<>(ValueType.NUMBER, function.apply(args));
    }
}
