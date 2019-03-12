package edu.kit.mima.core.instruction;

import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.interpretation.Value;
import edu.kit.mima.core.interpretation.ValueType;

import java.util.List;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class InstructionTools {

    private InstructionTools() {
        assert false : "utility class constructor";
    }

    /**
     * Check the argument for given number of arguments
     *
     * @param args                   arguments list
     * @param expectedArgumentNumber expected number of arguments
     */
    public static void checkArgNumber(List<Value> args, int expectedArgumentNumber) {
        if (args.size() != expectedArgumentNumber) {
            fail("invalid number of arguments");
        }
    }

    /**
     * Fetch an reference value from argument list
     *
     * @param arguments argument list
     * @param index     index of argument
     * @return reference argument value
     */
    public static Value getReferenceValue(List<Value> arguments, int index) {
        var argument = arguments.get(index);
        if (argument.getType() != ValueType.CONSTANT && argument.getType() != ValueType.NUMBER) {
            fail("can't pass a reference");
        }
        return argument;
    }

    /**
     * Check whether argument is a memory reference and if it is, whether the value is legal address
     *
     * @param arguments argument list
     * @param index     index of argument in list
     */
    public static Value getMemoryReference(List<Value> arguments, int index) {
        var argument = arguments.get(index);
        if (argument == null
                || !(argument.getType() == ValueType.NUMBER
                             || argument.getType() == ValueType.CONSTANT
                             || argument.getType() == ValueType.MEMORY_REFERENCE)) {
            fail("must pass a memory address");
        }
        if (!(argument.getType() == ValueType.MEMORY_REFERENCE)
                && ((MachineWord) (argument.getValue())).intValue() < 0) {
            fail("illegal memory address");
        }
        return argument;
    }

    /**
     * Fetch jump reference
     *
     * @param arguments argument list
     * @param index     index of argument in argument list
     * @return jump reference
     */
    public static Value getJumpReference(List<Value> arguments, int index) {
        var argument = arguments.get(index);
        if (argument.getType() != ValueType.JUMP_REFERENCE) {
            throw new IllegalArgumentException("must pass jump reference");
        }
        return argument;
    }


    /**
     * Throw error with given message
     *
     * @param message fail message
     */
    public static void fail(final String message) {
        throw new IllegalArgumentException(message);
    }
}
