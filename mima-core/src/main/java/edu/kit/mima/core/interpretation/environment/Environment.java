package edu.kit.mima.core.interpretation.environment;

import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.instruction.Instruction;
import edu.kit.mima.core.token.ProgramToken;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The Environment holds references to accessible memory variables, constants, functions and jump
 * points.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class Environment {
    @Nullable
    public static final Environment EMPTY_ENV = new Environment(null, null);

    @Nullable private final Environment parent;
    private final ProgramToken programToken;
    @NotNull private final HashMap<String, MachineWord> variables;
    @NotNull private final HashMap<String, MachineWord> constants;
    @NotNull private final HashMap<String, Instruction> functions;
    @NotNull private final HashMap<String, Integer> jumps;

    /**
     * Index of current expression.
     */
    private int expressionIndex;
    /**
     * When creating memory variables with no value given, an address is automatically assigned This
     * index is the current index of the last assigned value.
     */
    private int reservedIndex;

    /**
     * Create environment that inherits the fields from its parent environment.
     *
     * @param parent       parent Environment
     * @param programToken programToken for this environment
     */
    public Environment(@Nullable final Environment parent, final ProgramToken programToken) {
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
     * Get the variable and constants associations as follows {{variables},{constants}}.
     *
     * @return list of variables and constant associations maps
     */
    @NotNull
    public List<Map<String, MachineWord>> getDefinitions() {
        return List.of(variables, constants);
    }

    /**
     * Extend the current environment to new environment.
     *
     * @param programToken ProgramToken of extended environment
     * @return Environment with this environment as parent
     */
    @NotNull
    public Environment extend(final ProgramToken programToken) {
        return new Environment(this, programToken);
    }

    /**
     * Get the program Token of this environment.
     *
     * @return programToken of this environment
     */
    public ProgramToken getProgramToken() {
        return programToken;
    }

    /**
     * Returns the environment for which the given variable is defined.
     *
     * @param name name of variable
     * @return Environment with "name" defined. Null if name is not defined
     */
    @NotNull
    public Environment lookupVariable(final String name) {
        return lookup(env -> env.variables, name);

    }

    /**
     * Returns the environment for which the given constant is defined.
     *
     * @param name name of constant
     * @return Environment with "name" defined. Null if name is not defined
     */
    @NotNull
    public Environment lookupConstant(final String name) {
        return lookup(env -> env.constants, name);

    }

    /**
     * Returns the environment for which the given function is defined.
     *
     * @param name name of function
     * @return Environment with "name" defined. Null if name is not defined
     */
    @NotNull
    public Environment lookupFunction(final String name) {
        return lookup(env -> env.functions, name);
    }

    /**
     * Returns the environment for which the given jump is defined.
     *
     * @param name name of jump
     * @return Environment with "name" defined. Null if name is not defined
     */
    @NotNull
    public Environment lookupJump(final String name) {
        return lookup(env -> env.jumps, name);
    }

    @Nullable
    @Contract(pure = true)
    private <T> Environment lookup(@NotNull Function<Environment, Map<?, T>> mapFunction,
                                   String name) {
        Environment scope = this;
        while (scope != null) {
            if (mapFunction.apply(scope).containsKey(name)) {
                return scope;
            }
            scope = scope.parent;
        }
        return EMPTY_ENV;
    }

    /**
     * Get the current expression index.
     *
     * @return expression index
     */
    public int getExpressionIndex() {
        return expressionIndex;
    }

    /**
     * Set the expression index of the scope.
     *
     * @param expressionIndex expression index of scope
     */
    public void setExpressionIndex(final int expressionIndex) {
        assert expressionIndex >= 0 : "negative expression index";
        this.expressionIndex = expressionIndex;
    }

    /**
     * Get the value associated with "name" in the current scope.
     *
     * @param name name of variable
     * @return Integer associated with variable
     */
    public MachineWord getVariable(final String name) {
        return get(env -> env.variables, name);

    }

    /**
     * Get the constant value associated with "name" in the current scope.
     *
     * @param name name of variable
     * @return value associated with variable
     */
    public MachineWord getConstant(final String name) {
        return get(env -> env.constants, name);

    }

    /**
     * Get the function associated with "name" in the current scope.
     *
     * @param name name of variable
     * @return function associated with variable
     */
    public Instruction getFunction(final String name) {
        return get(env -> env.functions, name);

    }

    /**
     * Get the index of the expression associated with the given jum reference in the environment.
     *
     * @param name name of variable
     * @return index of expression in environment
     */
    @NotNull
    public Integer getJump(final String name) {
        return get(env -> env.jumps, name);
    }

    private <T> T get(@NotNull Function<Environment, Map<?, T>> mapFunction, String name) {
        if (mapFunction.apply(this).containsKey(name)) {
            return mapFunction.apply(this).get(name);
        }
        final Environment scope = lookup(mapFunction, name);
        if (scope == EMPTY_ENV) {
            throw new IllegalArgumentException("Undefined variable: " + name);
        }
        return scope.get(mapFunction, name);
    }


    /**
     * Return to the parent environment.
     *
     * @return the parent environment
     */
    @Nullable
    public Environment returnToParent() {
        return parent;
    }

    /**
     * Define a function for current environment.
     *
     * @param name     name of function
     * @param function function body
     */
    public void defineFunction(@NotNull final String name, final Instruction function) {
        define(name, function, functions, List.of(functions));

    }

    /**
     * Define a variable for current environment.
     *
     * @param name  name of variable
     * @param value value
     */
    public void defineVariable(@NotNull final String name, final MachineWord value) {
        define(name, value, variables, List.of(variables, constants));
    }

    /**
     * Define a constant for current environment.
     *
     * @param name  name of variable
     * @param value value
     */
    public void defineConstant(@NotNull final String name, final MachineWord value) {
        define(name, value, constants, List.of(variables, constants));
    }

    /**
     * Define a jumpPoint for current environment.
     *
     * @param name  name of jump point
     * @param index index of expression in Environment
     */
    public void defineJump(@NotNull final String name, final Integer index) {
        define(name, index, jumps, List.of(jumps));
    }

    /**
     * Define a value.
     *
     * @param key       key to define.
     * @param value     value for key.
     * @param putMap    map to put value in.
     * @param checkMaps maps to check if key is already defined.
     * @param <T>       Type of key
     * @param <K>       Type of value.
     */
    private <T, K> void define(@NotNull final T key, final K value,
                               @NotNull final Map<T, K> putMap,
                               @NotNull final List<Map<T, K>> checkMaps) {
        for (var m : checkMaps) {
            if (m.containsKey(key)) {
                throw new IllegalArgumentException(
                        "\"" + key.toString() + "\" already defined in scope");
            }
        }
        putMap.put(key, value);
    }

    /**
     * Get the reserved memory index.
     *
     * @return reserved memory index
     */
    public int getReservedIndex() {
        return reservedIndex;
    }

    /**
     * Set the reserved memory index.
     *
     * @param reservedIndex reserved memory index
     */
    public void setReservedIndex(final int reservedIndex) {
        this.reservedIndex = reservedIndex;
    }
}
