package edu.kit.mima.core.query;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Result of a Query
 *
 * @param <T> type of elements this query result holds
 * @author Jannis Weis
 */
public interface QueryResult<T> {

    /**
     * Start a new query on the output.
     *
     * @return Query
     */
    Query<T> and();

    /**
     * Start a new query on the prior output (or original data)
     * and keep the output of this result.
     *
     * @return Query
     */
    Query<T> or();

    /**
     * Invert the query result.
     *
     * @return sourceData \ foundData.
     */
    QueryResult<T> invert();

    /**
     * Get the query Result as a list
     *
     * @return List result
     */
    List<T> get();

    /**
     * Get the query Result as a list in the given ordering.
     *
     * @param comparator compare responsible for the ordering
     * @return List sorted of result
     */
    List<T> getSorted(Comparator<? super T> comparator);

    /**
     * Apply a function to each of the result.
     *
     * @param consumer Function to put the items of the output in.
     */
    void forEach(Consumer<T> consumer);

    /**
     * Get the first entry of the result query.
     *
     * @return QueryItem.
     */
    QueryItem<T> findFirst();

    /**
     * Return whether the query found no item.
     *
     * @return true if query didn't found any item.
     */
    boolean isEmpty();

    /**
     * Return whether the query found at least one or more items.
     *
     * @return true if query is not empty.
     */
    boolean anyMatch();
}
