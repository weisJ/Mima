package edu.kit.mima.core.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * QueryItem can be empty.
 *
 * @param <T> type of element this queryItem holds
 * @author Jannis Weis
 */
public interface QueryItem<T> {

    /**
     * Check whether the Item exists. Will return false if item is null.
     *
     * @return item != null
     */
    boolean exists();

    /**
     * If the result does not exist add a new Item instead.
     *
     * @param item new item to add
     * @return Item in QueryItem if exists, else will return the added item
     */
    @NotNull
    T orElseAdd(T item);

    /**
     * Get the content of this queryItem.
     *
     * @return item if query type
     * @throws IllegalRequestException if item does not exist
     */
    @Nullable
    T get() throws IllegalRequestException;

    /**
     * Get the content in this QueryItem. Else will throw an Exception. Behaves the same as get() but
     * The exception can be specified
     *
     * @param exceptionSupplier ExceptionSupplier
     * @param <X>               Type of Exception
     * @return item of query type
     * @throws X exception if item does not exist
     */
    @Nullable <X extends Throwable> T orElseThrow(Supplier<X> exceptionSupplier) throws X;
}
