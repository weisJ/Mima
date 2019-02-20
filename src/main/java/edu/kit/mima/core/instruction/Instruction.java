package edu.kit.mima.core.instruction;

import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.interpretation.Environment;
import edu.kit.mima.core.interpretation.Value;

import java.util.List;

/**
 * @author Jannis Weis
 * @since 2018
 */
public interface Instruction {

    /**
     * Apply the function
     *
     * @param arguments   function argument
     * @param environment runtime environment
     * @return return value of function
     */
    MachineWord apply(List<Value<MachineWord>> arguments, Environment environment);
}
