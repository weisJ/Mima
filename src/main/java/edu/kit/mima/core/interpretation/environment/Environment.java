package edu.kit.mima.core.interpretation.environment;

import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.instruction.Instruction;
import edu.kit.mima.core.parsing.token.ProgramToken;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Environment {

    private final @Nullable Environment parent;
    private final ProgramToken programToken;
    private final HashMap<String, MachineWord> variables;
    private final HashMap<String, MachineWord> constants;
    private final HashMap<String, Instruction> functions;
    private final HashMap<String, Integer> jumps;

    /**
     * Index of current expression
     */
    private int expressionIndex;
    /**
     * When creating memory variables with no value given, an address is automatically assigned
     * This index is the current index of the last assigned value.
     */
    private int reservedIndex;

    /**
     * Create environment that inherits the fields from its parent environment.
     *
     * @param parent       parent Environment
     * @param programToken programToken for this environment
     */
    public Environment(@Nullable Environment parent, ProgramToken programToken) {
        this.parent = parent;
        this.programToken = programToken;
        variables = new HashMap<>();
        constants = new HashMap<>();
        functions = new HashMap<>();
        jumps = new HashMap<>();
        expressionIndex = 0;
        reservedIndex = -1;
    }

    /**
     * Get the variable and constants associations as follows
     * {{variables},{constants}}
     *
     * @return list of variables and constant associations maps
     */
    public List<Map<String, MachineWord>> getDefinitions() {
        return List.of(variables, constants);
    }

    /**
     * Extend the current environment to new environment
     *
     * @param programToken ProgramToken of extended environment
     * @return Environment with this environment as parent
     */
    public Environment extend(ProgramToken programToken) {
        return new Environment(this, programToken);
    }

    /**
     * Get the program Token of this environment
     *
     * @return programToken of this environment
     */
    public ProgramToken getProgramToken() {
        return programToken;
    }

    /**
     * Returns the environment for which the given variable is defined
     *
     * @param name name of variable
     * @return Environment with "name" defined. Null if name is not defined
     */
    public @Nullable Environment lookupVariable(String name) {
        Environment scope = this;
        while (scope != null) {
            if (scope.variables.containsKey(name)) {
                return scope;
            }
            scope = scope.parent;
        }
        return null;
    }

    /**
     * Returns the environment for which the given constant is defined
     *
     * @param name name of constant
     * @return Environment with "name" defined. Null if name is not defined
     */
    public @Nullable Environment lookupConstant(String name) {
        Environment scope = this;
        while (scope != null) {
            if (scope.constants.containsKey(name)) {
                return scope;
            }
            scope = scope.parent;
        }
        return null;
    }

    /**
     * Returns the environment for which the given function is defined
     *
     * @param name name of function
     * @return Environment with "name" defined. Null if name is not defined
     */
    public @Nullable Environment lookupFunction(String name) {
        Environment scope = this;
        while (scope != null) {
            if (scope.functions.containsKey(name)) {
                return scope;
            }
            scope = scope.parent;
        }
        return null;
    }

    /**
     * Returns the environment for which the given jump is defined
     *
     * @param name name of jump
     * @return Environment with "name" defined. Null if name is not defined
     */
    public @Nullable Environment lookupJump(String name) {
        Environment scope = this;
        while (scope != null) {
            if (scope.jumps.containsKey(name)) {
                return scope;
            }
            scope = scope.parent;
        }
        return null;
    }

    /**
     * Get the current expression index
     *
     * @return expression index
     */
    public int getExpressionIndex() {
        return expressionIndex;
    }

    /**
     * Set the expression index of the scope
     *
     * @param expressionIndex expression index of scope
     */
    public void setExpressionIndex(int expressionIndex) {
        assert expressionIndex >= 0 : "negative expression index";
        this.expressionIndex = expressionIndex;
    }

    /**
     * Get the value associated with "name" in the current scope
     *
     * @param name name of variable
     * @return Integer associated with variable
     */
    public MachineWord getVariable(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        }
        Environment scope = lookupVariable(name);
        if (scope == null) {
            throw new IllegalArgumentException("Undefined variable: " + name);
        }
        return scope.getVariable(name);
    }

    /**
     * Get the constant value associated with "name" in the current scope
     *
     * @param name name of variable
     * @return value associated with variable
     */
    public MachineWord getConstant(String name) {
        if (constants.containsKey(name)) {
            return constants.get(name);
        }
        Environment scope = lookupConstant(name);
        if (scope == null) {
            throw new IllegalArgumentException("Undefined variable: " + name);
        }
        return scope.getConstant(name);
    }

    /**
     * Get the function associated with "name" in the current scope
     *
     * @param name name of variable
     * @return function associated with variable
     */
    public Instruction getFunction(String name) {
        if (functions.containsKey(name)) {
            return functions.get(name);
        }
        Environment scope = lookupFunction(name);
        if (scope == null) {
            throw new IllegalArgumentException("Undefined variable: " + name);
        }
        return scope.getFunction(name);
    }

    /**
     * Get the index of the expression associated with the given jum reference
     * in the environment
     *
     * @param name name of variable
     * @return index of expression in environment
     */
    public Integer getJump(String name) {
        if (jumps.containsKey(name)) {
            return jumps.get(name);
        }
        Environment scope = lookupJump(name);
        if (scope == null) {
            throw new IllegalArgumentException("Undefined variable: " + name);
        }
        return scope.getJump(name);
    }


    /**
     * Return to the parent environment
     *
     * @return the parent environment
     */
    public Environment returnToParent() {
        return parent;
    }

    /**
     * Define a function for current environment
     *
     * @param name     name of function
     * @param function function body
     */
    public void defineFunction(String name, Instruction function) {
        if (functions.containsKey(name)) {
            throw new IllegalArgumentException("function: \"" + name + "\" already defined in scope");
        }
        functions.put(name, function);
    }

    /**
     * Define a variable for current environment
     *
     * @param name  name of variable
     * @param value value
     */
    public void defineVariable(String name, MachineWord value) {
        if (variables.containsKey(name) || constants.containsKey(name)) {
            throw new IllegalArgumentException("reference: \"" + name + "\" already defined in scope");
        }
        variables.put(name, value);
    }

    /**
     * Define a constant for current environment
     *
     * @param name  name of variable
     * @param value value
     */
    public void defineConstant(String name, MachineWord value) {
        if (variables.containsKey(name) || constants.containsKey(name)) {
            throw new IllegalArgumentException("reference: \"" + name + "\" already defined in scope");
        }
        constants.put(name, value);
    }

    /**
     * Define a jumpPoint for current environment
     *
     * @param name  name of jump point
     * @param index index of expression in Environment
     */
    public void defineJump(String name, Integer index) {
        if (jumps.containsKey(name)) {
            throw new IllegalArgumentException("jump: \"" + name + "\" already defined in scope");
        }
        jumps.put(name, index);
    }

    /**
     * Get the reserved memory index
     *
     * @return reserved memory index
     */
    public int getReservedIndex() {
        return reservedIndex;
    }

    /**
     * Set the reserved memory index
     *
     * @param reservedIndex reserved memory index
     */
    public void setReservedIndex(int reservedIndex) {
        this.reservedIndex = reservedIndex;
    }
}
