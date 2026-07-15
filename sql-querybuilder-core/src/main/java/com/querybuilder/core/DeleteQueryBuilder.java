package com.querybuilder.core;

import java.util.ArrayList;
import java.util.List;

/**
 * DeleteQueryBuilder — builds: DELETE FROM table WHERE condition
 * Implements IMutationBuilder.
 *
 * Safety: throws IllegalStateException if WHERE is missing.
 * A DELETE without WHERE would wipe the ENTIRE table.
 *
 * SOLID:
 *   S — Single Responsibility: only builds DELETE SQL.
 *   L — Liskov: usable wherever IMutationBuilder is expected.
 */
public class DeleteQueryBuilder implements IMutationBuilder {

    private       String       table      = "";
    private final List<String> conditions = new ArrayList<>();
    private final List<Object> condValues = new ArrayList<>();

    /** Specifies the target table */
    public DeleteQueryBuilder from(String table) {
        this.table = table;
        return this;
    }

    /**
     * Adds a WHERE condition with its bound value.
     * Multiple calls are combined with AND.
     *
     * @param condition e.g. "id = ?"
     * @param value     the value bound to the ? placeholder
     */
    public DeleteQueryBuilder where(String condition, Object value) {
        conditions.add(condition);
        condValues.add(value);
        return this;
    }

    @Override
    public String build() {
        if (table == null || table.isBlank())
            throw new IllegalStateException(
                "Table required. Call .from(tableName)");
        if (conditions.isEmpty())
            throw new IllegalStateException(
                "DELETE without WHERE would wipe the entire table! Aborting. " +
                "Call .where(condition, value) to restrict the delete.");

        return "DELETE FROM " + table +
               " WHERE " + String.join(" AND ", conditions);
    }

    @Override
    public Object[] getValues() {
        return condValues.toArray();
    }

    @Override
    public void reset() {
        table = "";
        conditions.clear();
        condValues.clear();
    }
}
