package edu.kit.mima.core.query;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Query that can search through data.
 * Has similar (but not equal) to SQL structure
 *
 * @param <T> type of elements this query searches through
 * @author Jannis Weis
 */
public interface Query<T> {

    /**
     * Get results in query where the outcome of the function and the compare value are
     * equal
     *
     * @param function Function which takes in a value of the type of this query
     * @param compare  compare value. Should be the same type as the outcome of the function, but can be
     *                 of any type (if the query result should be empty)
     * @param <K>      Type of function return value
     * @return QueryResult
     */
    <K> QueryResult<T> whereEqual(Function<T, K> function, K compare);

    /**
     * Get results in query where the outcome of the function and the compare value are
     * not equal
     *
     * @param function Function which takes in a value of the type of this query
     * @param compare  compare value. Should be the same type as the outcome of the function, but can be
     *                 of any type (if the query result should contain all values)
     * @param <K>      Type of function return value
     * @return QueryResult
     */
    <K> QueryResult<T> whereNotEqual(Function<T, K> function, K compare);

    /**
     * Get the results for which the predicate returns true
     *
     * @param predicate Predicate that takes in the same type as this query type
     * @return QueryResult
     */
    QueryResult<T> where(Predicate<T> predicate);

    /**
     * Get the results for which the predicate returns false
     *
     * @param predicate Predicate that takes in the same type as this query type
     * @return QueryResult
     */
    QueryResult<T> whereNot(Predicate<T> predicate);

    /**
     * Get a query result with all values
     *
     * @return QueryResult
     */
    QueryResult<T> all();

    /**
     * Reset query for further use
     */
    void reset();
}
