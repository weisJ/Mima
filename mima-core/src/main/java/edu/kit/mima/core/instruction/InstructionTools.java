package edu.kit.mima.core.instruction;

import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.interpretation.Value;
import edu.kit.mima.core.interpretation.ValueType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Utility functions for {@link Instruction}s.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class InstructionTools {

    @Contract(" -> fail")
    private InstructionTools() {
        assert false : "utility class constructor";
    }

    /**
     * Check the argument for given number of arguments.
     *
     * @param args                   arguments list
     * @param expectedArgumentNumber expected number of arguments
     */
    public static void checkArgNumber(
            @NotNull final List<Value<?>> args, final int expectedArgumentNumber) {
        if (args.size() != expectedArgumentNumber) {
            fail("invalid number of arguments");
        }
    }

    /**
     * Fetch an reference value from argument list.
     *
     * @param arguments argument list
     * @param index     index of argument
     * @return reference argument value
     */
    public static Value<?> getReferenceValue(@NotNull final List<Value<?>> arguments, final int index) {
        final var argument = arguments.get(index);
        if (argument.getType() != ValueType.CONSTANT && argument.getType() != ValueType.NUMBER) {
            fail("can't pass a reference");
        }
        return argument;
    }

    /**
     * Check whether argument is a memory reference and if it is, whether the value is legal address.
     *
     * @param arguments argument list
     * @param index     index of argument in list
     * @return Value with memory reference
     */
    public static Value<?> getMemoryReference(@NotNull final List<Value<?>> arguments, final int index) {
        final var argument = arguments.get(index);
        if (argument == null
            || (argument.getType() != ValueType.NUMBER
                && argument.getType() != ValueType.CONSTANT
                && argument.getType() != ValueType.MEMORY_REFERENCE)) {
            fail("must pass a memory address");
        }
        if (!(argument.getType() == ValueType.MEMORY_REFERENCE)
            && ((MachineWord) (argument.getValue())).intValue() < 0) {
            fail("illegal memory address");
        }
        return argument;
    }

    /**
     * Fetch jump reference.
     *
     * @param arguments argument list
     * @param index     index of argument in argument list
     * @return jump reference
     */
    public static Value<?> getJumpReference(@NotNull final List<Value<?>> arguments, final int index) {
        final var argument = arguments.get(index);
        // jump can be referenced through number literal const reference or jump reference.
        if (argument.getType() != ValueType.JUMP_REFERENCE && argument.getType() != ValueType.CONSTANT
            && argument.getType() != ValueType.NUMBER) {
            throw new IllegalArgumentException("must pass jump reference");
        }
        return argument;
    }

    /**
     * Throw error with given message.
     *
     * @param message fail message
     */
    @Contract("_ -> fail")
    public static void fail(final String message) {
        throw new IllegalArgumentException(message);
    }
}
