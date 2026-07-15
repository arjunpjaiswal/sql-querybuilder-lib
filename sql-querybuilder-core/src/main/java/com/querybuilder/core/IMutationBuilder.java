package com.querybuilder.core;

/**
 * IMutationBuilder — common contract for INSERT, UPDATE, DELETE builders.
 *
 * SOLID applied:
 *   I — Interface Segregation: separate from IQueryBuilder (SELECT).
 *       Callers that only need mutation ops depend on this, not on the
 *       broader SELECT interface.
 *   D — Dependency Inversion: DAO classes depend on this interface,
 *       not on concrete builder classes.
 */
public interface IMutationBuilder {

    /** Assembles and returns the SQL string with ? placeholders */
    String build();

    /**
     * Returns the bound parameter values in the same order as the ? placeholders
     * produced by build(). Pass this array directly to QueryExecutor.
     */
    Object[] getValues();

    /** Clears all state so the builder instance can be reused */
    void reset();
}
