package edu.kit.mima.script.translator;

import edu.kit.mima.api.util.Tuple;
import edu.kit.mima.api.util.ValueTuple;
import edu.kit.mima.core.instruction.MimaInstruction;
import edu.kit.mima.core.instruction.MimaXInstruction;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.token.BinaryToken;
import edu.kit.mima.core.token.ListToken;
import edu.kit.mima.core.token.ProgramToken;
import edu.kit.mima.core.token.Token;
import edu.kit.mima.core.token.TokenType;
import edu.kit.mima.script.lang.Operation;
import edu.kit.mima.script.parser.ScriptParser;
import edu.kit.mima.script.parser.ScriptTokenStream;
import edu.kit.mima.script.token.BooleanToken;
import edu.kit.mima.script.token.ConditionalToken;
import edu.kit.mima.script.token.FunctionToken;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class ScriptTranslator {

    private static final String JMP = "__jmp%d__";
    private static final String RETURN = "RET();\n";
    private final Stack<Map<String, Integer>> variableStackMap;
    private final Stack<Integer> stackOffsets;
    private int globalStackOffset = 0;
    private int jmpIndex = 0;

    public ScriptTranslator() {
        variableStackMap = new Stack<>();
        stackOffsets = new Stack<>();
        variableStackMap.push(new HashMap<>());
        stackOffsets.push(0);
    }

    public String translate(final String input) {
        final ProgramToken programToken = new ScriptParser(new ScriptTokenStream(input)).parseTopLevel();
        return translateTopLevel(programToken);
    }

    @SuppressWarnings("unchecked")
    /*default*/ String translateTopLevel(final Token<?> token) {
        var type = token.getType();
        String ret = "";
        if (type == TokenType.PROGRAM) {
            ret = translateProgram((ProgramToken) token);
        } else if (type == TokenType.BINARY_EXPR) {
            ret =  translateBinaryExpr((BinaryToken<Token<Operation>, BinaryToken<Token<?>, Token<?>>>) token);
        } else if (type == TokenType.CONDITIONAL) {
            ret =  translateConditional((ConditionalToken) token);
        } else if (type == TokenType.UNARY) {
            ret =  translateUnary((BinaryToken<Token<Operation>, Token<?>>) token);
        } else if (type == TokenType.BINARY) {
            ret =  translateBinary(token);
        } else if (type == TokenType.NUMBER) {
            ret =  translateNumber(token);
        } else if (type == TokenType.BOOLEAN) {
            ret =  translateBoolean((BooleanToken) token);
        } else if (type == TokenType.FUNCTION) {
            ret =  translateFunction((FunctionToken) token);
        } else if (type == TokenType.CALL) {
            ret =  translateCall((BinaryToken<Token<?>, ListToken<Token<?>>>) token);
        } else if (type == TokenType.RETURN) {
            ret =  translateReturn((Token<Token<?>>) token);
        } else if (type == TokenType.IDENTIFICATION) {
            var variable = token.getValue().toString();
            var map = variableStackMap.peek();
            if (!map.containsKey(variable)) {
                throw new IllegalStateException("Variable " + variable + " is not declared in current scope");
            }
            ret =  MimaXInstruction.LDVR + "(" + (map.get(variable) - globalStackOffset) + ");\n";
        }
        return ret;
    }

    /*default*/ Tuple<String, Integer> translateIdentification(final Token<?> token) {
        var sb = new StringBuilder();
        var map = variableStackMap.peek();
        var variable = token.getValue().toString();
        if (!map.containsKey(variable)) {
            sb.append(defineVariable(variable));
        }
        return new ValueTuple<>(sb.toString(), map.get(variable) - globalStackOffset);
    }

    private String defineVariable(final String variable) {
        var map = variableStackMap.peek();
        map.put(variable, globalStackOffset);
        int stackOff = stackOffsets.pop();
        stackOffsets.push(stackOff + 1);
        return adjustStackPointer(1);
    }

    /*default*/ String adjustStackPointer(final int adjustment) {
        globalStackOffset += adjustment;
        return MimaXInstruction.LDSP + "(); "
               + MimaXInstruction.ADC + "(" + adjustment + "); "
               + MimaXInstruction.STSP + "();\n";
    }

    private String nextJump() {
        return String.format(JMP, jmpIndex++);
    }

    private void pushEnvironment() {
        variableStackMap.push(new HashMap<>(variableStackMap.peek()));
        stackOffsets.push(0);
    }

    private String popEnvironment() {
        variableStackMap.pop();
        int stackOff = stackOffsets.pop();
        return stackOff == 0 ? "" : adjustStackPointer(-stackOff);
    }


    private String translateBinaryExpr(final BinaryToken<Token<Operation>, BinaryToken<Token<?>, Token<?>>> token) {
        final var op = token.getFirst().getValue();
        final var args = token.getSecond();
        return OperationTranslator.getTranslator(op).translate(args.getFirst(), args.getSecond(), this);
    }

    private String translateConditional(final ConditionalToken token) {
        final var builder = new StringBuilder();
        builder.append(translateTopLevel(token.getCondition()));
        builder.append("\n");
        final var jmpIf = "__if" + nextJump();
        final var jmpEnd = "__endIf" + nextJump();
        builder.append("JMN(").append(jmpIf).append(");\n");
        //Else-Part
        if (token.hasElseBody()) {
            pushEnvironment();
            builder.append("\t").append(translateTopLevel(token.getElseBody())
                                                .replaceAll("\n", "\n\t"));
            builder.append("\t").append(popEnvironment());
        }
        builder.append("JMP(").append(jmpEnd).append(");\n");
        //Then-Part
        pushEnvironment();
        builder.append(jmpIf).append(Punctuation.JUMP_DELIMITER).append("\n\t");
        builder.append(translateTopLevel(token.getThenBody()).replaceAll("\n", "\n\t"));
        builder.append("\t").append(popEnvironment());
        //Endif
        builder.append(jmpEnd).append(Punctuation.JUMP_DELIMITER).append('\n');
        return builder.toString();
    }

    private String translateUnary(final BinaryToken<Token<Operation>, Token<?>> token) {
        final var op = token.getFirst().getValue();
        final var arg = token.getSecond();
        return OperationTranslator.getTranslator(op).translate(null, arg, this);
    }

    private String translateBinary(final Token<?> token) {
        return MimaInstruction.LDC + "(" + token.getValue() + ");\n";
    }

    private String translateNumber(final Token<?> token) {
        return MimaInstruction.LDC + "(" + token.getValue() + ");\n";
    }

    private String translateBoolean(final BooleanToken token) {
        if (token.getValue()) {
            // all ones (true)
            return MimaInstruction.LDC + "(-1);\n";
        } else {
            // all zeros (false)
            return MimaInstruction.LDC + "(0);\n";
        }
    }

    private String translateFunction(final FunctionToken token) {
        var params = token.getValue();
        var body = token.getBody();
        var sb = new StringBuilder();
        pushEnvironment();
        for (var par : params) {
            sb.append("# ").append(par.getValue()).append(' ').append(translateIdentification(par).getFirst());
        }
        String curr = "";
        for (var t : body.getValue()) {
            curr = translateTopLevel(t);
            sb.append(curr);
        }
        if (!curr.endsWith(RETURN)) {
            sb.append(popEnvironment());
            sb.append(RETURN);
        }
        return token.getName() + " " + Punctuation.JUMP_DELIMITER + "\n\t"
               + sb.toString().replaceAll("\n", "\n\t") + "\n";
    }

    private String translateReturn(final Token<Token<?>> token) {
        var sb = new StringBuilder();
//        var values = token.getValue().getValue();
//        sb.append(MimaInstruction.LDC).append("(").append(values.size()).append(");\n");
//        sb.append(MimaXInstruction.STVR).append("(0);\n");
//        sb.append(adjustStackPointer(1));
//        for (var par : values) {
//            sb.append(translateTopLevel(par));
//            sb.append(MimaXInstruction.STVR).append("(0);\n");
//            sb.append(adjustStackPointer(1));
//        }
//        sb.append(adjustStackPointer(-1 * (values.size() + 1)));
//        sb.append(MimaXInstruction.SP).append("();\n");
        var value = token.getValue();
        sb.append(translateTopLevel(value));
        sb.append(popEnvironment());
        sb.append(RETURN);
        return sb.toString();
    }

    private String translateCall(final BinaryToken<Token<?>, ListToken<Token<?>>> token) {
        var name = token.getFirst().getValue().toString();
        var params = token.getSecond().getValue();
        var sb = new StringBuilder();
        for (var par : params) {
            sb.append(translateTopLevel(par));
            sb.append(MimaXInstruction.STVR).append("(0);\n");
            sb.append(adjustStackPointer(1));
        }
        sb.append(adjustStackPointer(-1 * params.size()));
        sb.append("CALL(").append(name).append(");\n");
        return sb.toString();
    }

    private String translateProgram(final ProgramToken token) {
        var sb = new StringBuilder();
        pushEnvironment();
        for (var t : token.getValue()) {
            sb.append(translateTopLevel(t)).append("#---#\n");
        }
        sb.append(popEnvironment());
        return sb.toString();
    }
}
