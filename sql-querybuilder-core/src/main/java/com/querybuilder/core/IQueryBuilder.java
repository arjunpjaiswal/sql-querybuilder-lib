package com.querybuilder.core;

/**
 * IQueryBuilder — Builder Interface for SELECT statements.
 *
 * Defines the contract every SELECT-style query builder must fulfill.
 * Every method returns IQueryBuilder (i.e., 'this') to enable fluent
 * method chaining: .select().from().where().build()
 *
 * SOLID:
 *   I — Interface Segregation: this interface covers only SELECT queries.
 *       INSERT/UPDATE/DELETE have their own dedicated builders (IMutationBuilder).
 *   L — Liskov Substitution: any concrete implementation can replace this type.
 */
public interface IQueryBuilder {

    /** SELECT col1, col2, ... (pass "*" or omit for all columns) */
    IQueryBuilder select(String... columns);

    /** FROM tableName (may include alias, e.g. "users u") */
    IQueryBuilder from(String table);

    /** WHERE condition (resets any prior WHERE) */
    IQueryBuilder where(String condition);

    /** AND condition (appended after WHERE or a prior AND) */
    IQueryBuilder and(String condition);

    /** OR condition */
    IQueryBuilder or(String condition);

    /** INNER JOIN table ON condition */
    IQueryBuilder innerJoin(String table, String on);

    /** LEFT JOIN table ON condition */
    IQueryBuilder leftJoin(String table, String on);

    /** RIGHT JOIN table ON condition */
    IQueryBuilder rightJoin(String table, String on);

    /** GROUP BY col1, col2, ... */
    IQueryBuilder groupBy(String... columns);

    /** HAVING condition (requires prior groupBy call) */
    IQueryBuilder having(String condition);

    /** ORDER BY column direction — can be called multiple times to add secondary sorts */
    IQueryBuilder orderBy(String column, String direction);

    /** LIMIT n */
    IQueryBuilder limit(int n);

    /** OFFSET n */
    IQueryBuilder offset(int n);

    /** Adds DISTINCT after SELECT */
    IQueryBuilder distinct();

    /** Terminal operation — assembles and returns the final SQL string */
    String build();

    /** Clears all state so the builder instance can be reused */
    void reset();
}
