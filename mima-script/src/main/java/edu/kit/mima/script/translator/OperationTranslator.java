package edu.kit.mima.script.translator;

import edu.kit.mima.api.lambda.TriFunction;
import edu.kit.mima.core.instruction.MimaXInstruction;
import edu.kit.mima.core.token.Token;
import edu.kit.mima.script.lang.Operation;

/**
 * @author Jannis Weis
 * @since 2019
 */
public enum OperationTranslator {
    ASSIGN((l, r, t) -> {
        var sb = new StringBuilder();
        var left = t.translateIdentification(l);
        sb.append(left.getFirst());
        sb.append(t.translateTopLevel(r));
        sb.append(MimaXInstruction.STVR).append("(").append(left.getSecond()).append(");\n");
        return sb.toString();
    }),
    OR((l, r, t) -> binary("op||", l, r, t)),
    AND_BIT((l, r, t) -> binary("op&", l, r, t)),
    OR_BIT((l, r, t) -> binary("op|", l, r, t)),
    XOR((l, r, t) -> binary("op^", l, r, t)),
    AND((l, r, t) -> binary("op&&", l, r, t)),

    PLUS((l, r, t) -> binary("op+", l, r, t)),
    MINUS((l, r, t) -> binary("op-", l, r, t)),
    MULTIPLY((l, r, t) -> binary("op*", l, r, t)),
    DIVIDE((l, r, t) -> binary("op/", l, r, t)),
    MODULO((l, r, t) -> binary("op%", l, r, t)),

    SHIFT_LEFT((l, r, t) -> binary("op<<", l, r, t)),
    SHIFT_RIGHT((l, r, t) -> binary("op>>", l, r, t)),

    NOT((l, r, t) -> unary("un!", r, t)),

    UNARY_PLUS((l, r, t) -> unary("un+", r, t)),
    UNARY_MINUS((l, r, t) -> unary("un-", r, t)),

    EQUAL((l, r, t) -> binary("op==", l, r, t)),
    NOT_EQUAL((l, r, t) -> binary("op!=", l, r, t)),

    LESS_THAN((l, r, t) -> binary("op<", l, r, t)),
    GREATER_THAN((l, r, t) -> binary("op>", l, r, t)),
    LESS_THAN_OR_EQU((l, r, t) -> binary("op<=", l, r, t)),
    GREATER_THAN_OR_EQU((l, r, t) -> binary("op>=", l, r, t));

    private final TriFunction<Token<?>, Token<?>, ScriptTranslator, String> translator;

    OperationTranslator(final TriFunction<Token<?>, Token<?>, ScriptTranslator, String> translator) {
        this.translator = translator;
    }

    private static String binary(final String operator, final Token<?> left, final Token<?> right,
                                 final ScriptTranslator t) {
        return t.translateTopLevel(left)
               + MimaXInstruction.STVR + "(0);\n"
               + t.adjustStackPointer(1)
               + t.translateTopLevel(right)
               + MimaXInstruction.STVR + "(0);\n"
               + t.adjustStackPointer(-1)
               + "CALL(" + operator + ");\n";
    }

    private static String unary(final String instruction, final Token<?> token, final ScriptTranslator t) {
        return t.translateTopLevel(token)
               + MimaXInstruction.STVR + "(0);\n"
               + "CALL(" + instruction + ")\n";
    }

    public static OperationTranslator getTranslator(final Operation op) {
        return switch (op) {
            case ASSIGN -> OperationTranslator.ASSIGN;
            case OR -> OperationTranslator.OR;
            case AND_BIT -> OperationTranslator.AND_BIT;
            case OR_BIT -> OperationTranslator.OR_BIT;
            case XOR -> OperationTranslator.XOR;
            case AND -> OperationTranslator.AND;
            case EQUAL -> OperationTranslator.EQUAL;
            case NOT_EQUAL -> OperationTranslator.NOT_EQUAL;
            case LESS_THAN -> OperationTranslator.LESS_THAN;
            case GREATER_THAN -> OperationTranslator.GREATER_THAN;
            case LESS_THAN_OR_EQU -> OperationTranslator.LESS_THAN_OR_EQU;
            case GREATER_THAN_OR_EQU -> OperationTranslator.GREATER_THAN_OR_EQU;
            case SHIFT_LEFT -> OperationTranslator.SHIFT_LEFT;
            case SHIFT_RIGHT -> OperationTranslator.SHIFT_RIGHT;
            case PLUS -> OperationTranslator.PLUS;
            case MINUS -> OperationTranslator.MINUS;
            case MULTIPLY -> OperationTranslator.MULTIPLY;
            case DIVIDE -> OperationTranslator.DIVIDE;
            case MODULO -> OperationTranslator.MODULO;
            case NOT -> OperationTranslator.NOT;
            case UNARY_PLUS -> OperationTranslator.UNARY_PLUS;
            case UNARY_MINUS -> OperationTranslator.UNARY_MINUS;
        };
    }

    /**
     * Translate the operation to Mima Code.
     * If the operation is unary, only the right token will be used.
     *
     * @param left             left token.
     * @param right            right token.
     * @param scriptTranslator translator unit.
     * @return Mima COde as script. Result will be stored in the accumulator.
     */
    String translate(final Token<?> left, final Token<?> right, final ScriptTranslator scriptTranslator) {
        return translator.apply(left, right, scriptTranslator);
    }
}
