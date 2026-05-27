package com.querybuilder.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * UpdateQueryBuilder — builds: UPDATE table SET col=? WHERE condition
 * Implements IMutationBuilder.
 *
 * Safety: throws IllegalStateException if WHERE is missing.
 * An UPDATE without WHERE would update EVERY row in the table.
 *
 * SOLID:
 *   S — Single Responsibility: only builds UPDATE SQL.
 *   L — Liskov: usable wherever IMutationBuilder is expected.
 */
public class UpdateQueryBuilder implements IMutationBuilder {

    private       String             table      = "";
    private final Map<String,Object> setClauses = new LinkedHashMap<>();
    private final List<String>       conditions = new ArrayList<>();
    private final List<Object>       condValues = new ArrayList<>();

    /** Specifies the target table */
    public UpdateQueryBuilder update(String table) {
        this.table = table;
        return this;
    }

    /** Adds a SET col = ? clause */
    public UpdateQueryBuilder set(String column, Object value) {
        setClauses.put(column, value);
        return this;
    }

    /**
     * Adds a WHERE condition with its bound value.
     * Multiple calls are combined with AND.
     *
     * @param condition e.g. "id = ?"
     * @param value     the value bound to the ? placeholder
     */
    public UpdateQueryBuilder where(String condition, Object value) {
        conditions.add(condition);
        condValues.add(value);
        return this;
    }

    @Override
    public String build() {
        if (table == null || table.isBlank())
            throw new IllegalStateException(
                "Table required. Call .update(tableName)");
        if (setClauses.isEmpty())
            throw new IllegalStateException(
                "Nothing to update. Call .set(column, value) at least once.");
        if (conditions.isEmpty())
            throw new IllegalStateException(
                "UPDATE without WHERE is too dangerous — it would affect ALL rows! " +
                "Call .where(condition, value) to restrict the update.");

        List<String> setParts = new ArrayList<>();
        for (String col : setClauses.keySet()) setParts.add(col + " = ?");

        return "UPDATE " + table +
               " SET " + String.join(", ", setParts) +
               " WHERE " + String.join(" AND ", conditions);
    }

    /**
     * CRITICAL: SET values come before WHERE values — this matches the
     * order of ? placeholders in the SQL produced by build().
     */
    @Override
    public Object[] getValues() {
        List<Object> all = new ArrayList<>(setClauses.values());
        all.addAll(condValues);
        return all.toArray();
    }

    @Override
    public void reset() {
        table = "";
        setClauses.clear();
        conditions.clear();
        condValues.clear();
    }
}
