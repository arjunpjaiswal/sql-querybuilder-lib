package com.querybuilder.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SelectQueryBuilder — Concrete Builder for SELECT statements.
 *
 * Each setter method stores its value into a private field and returns 'this'
 * (enabling method chaining). The build() method is the only place where all
 * pieces are assembled into a final SQL string.
 *
 * SOLID applied:
 *   S — Single Responsibility: only builds SELECT SQL, nothing else.
 *   O — Open/Closed: new clauses can be added without breaking existing ones.
 *   L — Liskov: can be used wherever IQueryBuilder is expected.
 *   I — Interface Segregation: implements only SELECT-relevant methods.
 */
public class SelectQueryBuilder implements IQueryBuilder {

    // ── Internal state — each field maps to one SQL clause ──────────────────
    private final List<String> columns        = new ArrayList<>();
    private       String       table          = "";
    private final List<String> conditions     = new ArrayList<>();
    private final List<String> joinClauses    = new ArrayList<>();
    private final List<String> groupByCols    = new ArrayList<>();
    private       String       havingClause   = "";
    private final List<String> orderByClauses = new ArrayList<>();
    private       int          limitVal       = -1;  // -1 means not set
    private       int          offsetVal      = -1;
    private       boolean      isDistinct     = false;

    // ── SELECT ───────────────────────────────────────────────────────────────
    @Override
    public SelectQueryBuilder select(String... cols) {
        if (cols.length == 0) {
            columns.add("*");
        } else {
            columns.addAll(Arrays.asList(cols));
        }
        return this;  // returns 'this' — enables chaining
    }

    @Override
    public SelectQueryBuilder distinct() {
        this.isDistinct = true;
        return this;
    }

    // ── FROM ─────────────────────────────────────────────────────────────────
    @Override
    public SelectQueryBuilder from(String table) {
        this.table = table;
        return this;
    }

    // ── WHERE / AND / OR ─────────────────────────────────────────────────────
    @Override
    public SelectQueryBuilder where(String condition) {
        conditions.clear();       // reset any previous conditions
        conditions.add(condition);
        return this;
    }

    @Override
    public SelectQueryBuilder and(String condition) {
        conditions.add("AND " + condition);
        return this;
    }

    @Override
    public SelectQueryBuilder or(String condition) {
        conditions.add("OR " + condition);
        return this;
    }

    // ── JOIN ─────────────────────────────────────────────────────────────────
    @Override
    public SelectQueryBuilder innerJoin(String tbl, String on) {
        joinClauses.add("INNER JOIN " + tbl + " ON " + on);
        return this;
    }

    @Override
    public SelectQueryBuilder leftJoin(String tbl, String on) {
        joinClauses.add("LEFT JOIN " + tbl + " ON " + on);
        return this;
    }

    @Override
    public SelectQueryBuilder rightJoin(String tbl, String on) {
        joinClauses.add("RIGHT JOIN " + tbl + " ON " + on);
        return this;
    }

    // ── GROUP BY / HAVING ────────────────────────────────────────────────────
    @Override
    public SelectQueryBuilder groupBy(String... cols) {
        groupByCols.addAll(Arrays.asList(cols));
        return this;
    }

    @Override
    public SelectQueryBuilder having(String condition) {
        this.havingClause = condition;
        return this;
    }

    // ── ORDER BY ─────────────────────────────────────────────────────────────
    @Override
    public SelectQueryBuilder orderBy(String column, String direction) {
        // Whitelist direction to prevent injection via this field
        String dir = "DESC".equalsIgnoreCase(direction) ? "DESC" : "ASC";
        orderByClauses.add(column + " " + dir);
        return this;
    }

    // ── LIMIT / OFFSET ───────────────────────────────────────────────────────
    @Override
    public SelectQueryBuilder limit(int n) {
        this.limitVal = n;
        return this;
    }

    @Override
    public SelectQueryBuilder offset(int n) {
        this.offsetVal = n;
        return this;
    }

    // ── BUILD — assembles all state into final SQL string ────────────────────
    @Override
    public String build() {
        validate();  // fail fast if required fields are missing

        StringBuilder sql = new StringBuilder("SELECT ");

        if (isDistinct) sql.append("DISTINCT ");
        sql.append(columns.isEmpty() ? "*" : String.join(", ", columns));
        sql.append(" FROM ").append(table);

        if (!joinClauses.isEmpty())
            sql.append(" ").append(String.join(" ", joinClauses));
        if (!conditions.isEmpty())
            sql.append(" WHERE ").append(String.join(" ", conditions));
        if (!groupByCols.isEmpty())
            sql.append(" GROUP BY ").append(String.join(", ", groupByCols));
        if (!havingClause.isEmpty())
            sql.append(" HAVING ").append(havingClause);
        if (!orderByClauses.isEmpty())
            sql.append(" ORDER BY ").append(String.join(", ", orderByClauses));
        if (limitVal > 0)
            sql.append(" LIMIT ").append(limitVal);
        if (offsetVal >= 0)
            sql.append(" OFFSET ").append(offsetVal);

        return sql.toString();
    }

    // ── VALIDATE — called before build() assembles anything ──────────────────
    private void validate() {
        if (table == null || table.isBlank())
            throw new IllegalStateException(
                "FROM clause is required — call .from(tableName)");
        if (!havingClause.isEmpty() && groupByCols.isEmpty())
            throw new IllegalStateException(
                "HAVING requires GROUP BY — call .groupBy(columns) first");
    }

    // ── RESET — clears all state so builder can be reused ────────────────────
    @Override
    public void reset() {
        columns.clear();
        table = "";
        conditions.clear();
        joinClauses.clear();
        groupByCols.clear();
        havingClause = "";
        orderByClauses.clear();
        limitVal  = -1;
        offsetVal = -1;
        isDistinct = false;
    }
}
