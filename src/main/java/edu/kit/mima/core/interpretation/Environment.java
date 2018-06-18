package edu.kit.mima.core.interpretation;

import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.instruction.Instruction;
import edu.kit.mima.core.parsing.token.ProgramToken;
import edu.kit.mima.core.parsing.token.Token;
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
    private final HashMap<Token, MachineWord> variables;
    private final HashMap<Token, MachineWord> constants;
    private final HashMap<Token, Instruction> functions;
    private final HashMap<Token, Integer> jumps;

    private int expressionIndex;

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
    }

    /**
     * Get the variable and constants associations as follows
     * {{variables},{constants}}
     *
     * @return list of variables and constant associations maps
     */
    public List<Map<Token, MachineWord>> getDefinitions() {
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
    public @Nullable Environment lookupVariable(Token name) {
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
    public @Nullable Environment lookupConstant(Token name) {
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
    public @Nullable Environment lookupFunction(Token name) {
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
    public @Nullable Environment lookupJump(Token name) {
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
    public MachineWord getVariable(Token name) {
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
    public MachineWord getConstant(Token name) {
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
    public Instruction getFunction(Token name) {
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
    public Integer getJump(Token name) {
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
        if (parent == null) {
            throw new IllegalStateException("No parent environment defined");
        }
        return parent;
    }

    /**
     * Define a function for current environment
     *
     * @param name     name of function
     * @param function function body
     */
    public void defineFunction(Token name, Instruction function) {
        if (functions.containsKey(name)) {
            throw new IllegalArgumentException("function: \"" + name.getValue() + "\" already defined in scope");
        }
        functions.put(name, function);
    }

    /**
     * Define a variable for current environment
     *
     * @param name  name of variable
     * @param value value
     */
    public void defineVariable(Token name, MachineWord value) {
        if (variables.containsKey(name) || constants.containsKey(name)) {
            throw new IllegalArgumentException("reference: \"" + name.getValue() + "\" already defined in scope");
        }
        variables.put(name, value);
    }

    /**
     * Define a constant for current environment
     *
     * @param name  name of variable
     * @param value value
     */
    public void defineConstant(Token name, MachineWord value) {
        if (variables.containsKey(name) || constants.containsKey(name)) {
            throw new IllegalArgumentException("reference: \"" + name.getValue() + "\" already defined in scope");
        }
        constants.put(name, value);
    }

    /**
     * Define a jumpPoint for current environment
     *
     * @param name  name of jump point
     * @param index index of expression in Environment
     */
    public void defineJump(Token name, Integer index) {
        if (jumps.containsKey(name)) {
            throw new IllegalArgumentException("jump: \"" + name.getValue() + "\" already defined in scope");
        }
        jumps.put(name, index);
    }

}
