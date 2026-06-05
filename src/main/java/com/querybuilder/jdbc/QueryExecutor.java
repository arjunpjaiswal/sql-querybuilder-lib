package com.querybuilder.jdbc;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * QueryExecutor — executes SQL strings produced by the QueryBuilders.
 *
 * Always uses PreparedStatement — never Statement — to prevent SQL Injection.
 * All user-supplied values are bound via ? placeholders using setObject().
 *
 * SOLID:
 *   S — Single Responsibility: only executes SQL, never builds it.
 *   D — Dependency Inversion: depends on java.sql.Connection (interface),
 *       not on any concrete vendor driver class.
 */
public class QueryExecutor {

    private final Connection connection;

    public QueryExecutor() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * executeQuery — for SELECT statements.
     *
     * Returns a List of rows; each row is a LinkedHashMap<columnName, value>.
     * LinkedHashMap preserves column order as returned by the database.
     *
     * Uses try-with-resources so PreparedStatement and ResultSet are always
     * closed even if an exception is thrown mid-way — no resource leaks.
     *
     * @param sql    SQL string with ? placeholders produced by a builder
     * @param params values bound to each ? in left-to-right order
     */
    public List<Map<String, Object>> executeQuery(String sql, Object... params)
            throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bindParams(ps, params);
            System.out.println("[SQL] " + sql);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= colCount; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    results.add(row);
                }
            }
        }
        return results;
    }

    /**
     * executeUpdate — for INSERT, UPDATE, DELETE statements.
     * Returns the number of rows affected.
     *
     * @param sql    SQL string with ? placeholders
     * @param params values bound to each ?
     */
    public int executeUpdate(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bindParams(ps, params);
            System.out.println("[SQL] " + sql);
            return ps.executeUpdate();
        }
    }

    /**
     * executeInsertGetKey — INSERT and return the auto-generated primary key.
     *
     * Passes Statement.RETURN_GENERATED_KEYS as a flag so JDBC retrieves
     * the auto-incremented PK after the INSERT completes.
     *
     * @param sql    INSERT SQL with ? placeholders
     * @param params values bound to each ?
     * @return       the generated primary key value
     */
    public long executeInsertGetKey(String sql, Object... params)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            bindParams(ps, params);
            System.out.println("[SQL] " + sql);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
        }
        throw new SQLException("INSERT succeeded but no generated key was returned.");
    }

    // ── Binds each param to its ? placeholder; JDBC index is 1-based ─────────
    private void bindParams(PreparedStatement ps, Object[] params)
            throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

}
