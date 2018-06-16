package edu.kit.mima.core.interpretation;

import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.parsing.token.ProgramToken;
import edu.kit.mima.core.parsing.token.Token;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Environment {

    private final @Nullable Environment parent;
    private final ProgramToken programToken;
    private final HashMap<Token, MachineWord> variables;
    private final HashMap<Token, MachineWord> constants;
    private final HashMap<Token, Function<List<Value<MachineWord>>, MachineWord>> functions;
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
        this.variables = new HashMap<>();
        this.constants = new HashMap<>();
        this.functions = new HashMap<>();
        this.jumps = new HashMap<>();
        expressionIndex = 0;
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
     * Returns the environment for which the given name is defined
     * either as a function or a variable
     *
     * @param name name of function or variable
     * @return Environment with "name" defined. Null if name is not defined
     */
    public Environment lookup(Token name) {
        Environment scope = this;
        while (scope != null) {
            if (scope.variables.containsKey(name)
                    || scope.constants.containsKey(name)
                    || scope.functions.containsKey(name)
                    || scope.jumps.containsKey(name)) {
                return scope;
            }
            scope = scope.parent;
        }
        return null;
    }

    public int getExpressionIndex() {
        return expressionIndex;
    }

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
        } else {
            Environment scope = lookup(name);
            if (scope == null) {
                throw new IllegalArgumentException("Undefined variable: " + name);
            }
            return scope.getVariable(name);
        }
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
        } else {
            Environment scope = lookup(name);
            if (scope == null) {
                throw new IllegalArgumentException("Undefined variable: " + name);
            }
            return scope.getConstant(name);
        }
    }

    /**
     * Get the function associated with "name" in the current scope
     *
     * @param name name of variable
     * @return function associated with variable
     */
    public Function<List<Value<MachineWord>>, MachineWord> getFunction(Token name) {
        if (functions.containsKey(name)) {
            return functions.get(name);
        } else {
            Environment scope = lookup(name);
            if (scope == null) {
                throw new IllegalArgumentException("Undefined variable: " + name);
            }
            return scope.getFunction(name);
        }
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
        } else {
            Environment scope = lookup(name);
            if (scope == null) {
                throw new IllegalArgumentException("Undefined variable: " + name);
            }
            return scope.getJump(name);
        }
    }

    /**
     * Set the value of the given variable
     *
     * @param name  name of variable
     * @param value new value of variable
     */
    public void setVariable(Token name, MachineWord value) {
        Environment scope = this.lookup(name);
        if (scope == null) {
            throw new IllegalArgumentException("Undefined variable: " + name);
        }
        if (scope.parent == null) {
            throw new IllegalArgumentException("Can't override global variables");
        }
        scope.variables.put(name, value);
    }

    /**
     * Set the function body of the given variable
     *
     * @param name     name of variable
     * @param function new function body of variable
     */
    public void setFunction(Token name, Function<List<Value<MachineWord>>, MachineWord> function) {
        Environment scope = this.lookup(name);
        if (scope == null) {
            throw new IllegalArgumentException("Undefined function: " + name);
        }
        if (scope.parent == null) {
            throw new IllegalArgumentException("Can't override global function");
        }
        scope.functions.put(name, function);
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
        return this.parent;
    }

    /**
     * Define a function for current environment
     *
     * @param name     name of function
     * @param function function body
     * @return true if there was no previous binding to the function name
     */
    public boolean defineFunction(Token name, Function<List<Value<MachineWord>>, MachineWord> function) {
        return this.functions.put(name, function) != null;
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
        this.variables.put(name, value);
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
        this.constants.put(name, value);
    }

    /**
     * Define a jumpPoint for current environment
     *
     * @param name  name of jump point
     * @param index index of expression in Environment
     */
    public void defineJump(Token name, Integer index) {
        if (functions.containsKey(name)) {
            throw new IllegalArgumentException("function: \"" + name.getValue() + "\" already defined in scope");
        }
        this.jumps.put(name, index);
    }

}
