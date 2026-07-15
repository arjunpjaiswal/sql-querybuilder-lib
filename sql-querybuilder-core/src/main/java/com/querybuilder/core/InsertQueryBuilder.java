package com.querybuilder.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * InsertQueryBuilder — builds: INSERT INTO table (col1, col2) VALUES (?, ?)
 * Implements IMutationBuilder.
 *
 * Builder Pattern: fluent .into().set().set().build()
 *
 * SOLID:
 *   S — Single Responsibility: only builds INSERT SQL.
 *   L — Liskov: usable wherever IMutationBuilder is expected.
 *   I — Interface Segregation: implements only mutation-relevant contract.
 */
public class InsertQueryBuilder implements IMutationBuilder {

    private String table = "";

    /**
     * LinkedHashMap preserves insertion order — critical for matching
     * column names to ? placeholders in the VALUES list.
     */
    private final Map<String, Object> columns = new LinkedHashMap<>();

    /** Specifies the target table */
    public InsertQueryBuilder into(String table) {
        this.table = table;
        return this;
    }

    /** Adds a column-value pair to the INSERT statement */
    public InsertQueryBuilder set(String column, Object value) {
        columns.put(column, value);
        return this;
    }

    @Override
    public String build() {
        if (table == null || table.isBlank())
            throw new IllegalStateException(
                "Table name is required. Call .into(tableName)");
        if (columns.isEmpty())
            throw new IllegalStateException(
                "No columns specified. Call .set(column, value) at least once.");

        String colList      = String.join(", ", columns.keySet());
        String placeholders = String.join(", ",
            Collections.nCopies(columns.size(), "?"));

        return "INSERT INTO " + table +
               " (" + colList + ") VALUES (" + placeholders + ")";
    }

    /**
     * Returns values in the same order as keySet() — matches ? placeholder order.
     * Pass this directly to QueryExecutor.executeInsertGetKey() or executeUpdate().
     */
    @Override
    public Object[] getValues() {
        return columns.values().toArray();
    }

    @Override
    public void reset() {
        table = "";
        columns.clear();
    }
}
