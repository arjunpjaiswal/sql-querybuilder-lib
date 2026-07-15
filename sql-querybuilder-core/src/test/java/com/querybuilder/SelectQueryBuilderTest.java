package com.querybuilder;

import com.querybuilder.core.SelectQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SelectQueryBuilder.
 * No database required — tests the SQL string assembly logic only.
 */
@DisplayName("SelectQueryBuilder Tests")
class SelectQueryBuilderTest {

    private SelectQueryBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new SelectQueryBuilder();
    }

    // ── Basic SELECT ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("SELECT * FROM table")
    void testSelectStar() {
        String sql = builder.select("*").from("users").build();
        assertEquals("SELECT * FROM users", sql);
    }

    @Test
    @DisplayName("SELECT specific columns")
    void testSelectColumns() {
        String sql = builder.select("id", "name", "email").from("users").build();
        assertEquals("SELECT id, name, email FROM users", sql);
    }

    @Test
    @DisplayName("SELECT DISTINCT")
    void testSelectDistinct() {
        String sql = builder.distinct().select("city").from("users").build();
        assertEquals("SELECT DISTINCT city FROM users", sql);
    }

    @Test
    @DisplayName("FROM is required — missing FROM throws")
    void testMissingFromThrows() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> builder.select("id").build());
        assertTrue(ex.getMessage().contains("FROM"));
    }

    // ── WHERE / AND / OR ─────────────────────────────────────────────────────

    @Test
    @DisplayName("WHERE single condition")
    void testWhere() {
        String sql = builder.select("*").from("users").where("id = ?").build();
        assertEquals("SELECT * FROM users WHERE id = ?", sql);
    }

    @Test
    @DisplayName("WHERE ... AND ...")
    void testWhereAnd() {
        String sql = builder.select("*").from("users")
                .where("age > ?").and("city = ?").build();
        assertEquals("SELECT * FROM users WHERE age > ? AND city = ?", sql);
    }

    @Test
    @DisplayName("WHERE ... OR ...")
    void testWhereOr() {
        String sql = builder.select("*").from("users")
                .where("city = ?").or("city = ?").build();
        assertEquals("SELECT * FROM users WHERE city = ? OR city = ?", sql);
    }

    @Test
    @DisplayName("WHERE resets on second call")
    void testWhereResets() {
        String sql = builder.select("*").from("users")
                .where("old = ?").where("new = ?").build();
        assertEquals("SELECT * FROM users WHERE new = ?", sql);
    }

    // ── JOIN ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("INNER JOIN")
    void testInnerJoin() {
        String sql = builder.select("u.name", "o.total_price")
                .from("users u")
                .innerJoin("orders o", "o.user_id = u.id")
                .build();
        assertEquals(
            "SELECT u.name, o.total_price FROM users u INNER JOIN orders o ON o.user_id = u.id",
            sql);
    }

    @Test
    @DisplayName("LEFT JOIN")
    void testLeftJoin() {
        String sql = builder.select("u.name", "COUNT(o.id) AS cnt")
                .from("users u")
                .leftJoin("orders o", "o.user_id = u.id")
                .groupBy("u.id", "u.name")
                .build();
        assertTrue(sql.contains("LEFT JOIN orders o ON o.user_id = u.id"));
    }

    @Test
    @DisplayName("Multiple JOINs chain correctly")
    void testMultipleJoins() {
        String sql = builder.select("u.name", "p.name", "o.quantity")
                .from("users u")
                .innerJoin("orders o",   "o.user_id = u.id")
                .innerJoin("products p", "p.id = o.product_id")
                .build();
        assertTrue(sql.contains("INNER JOIN orders o ON o.user_id = u.id"));
        assertTrue(sql.contains("INNER JOIN products p ON p.id = o.product_id"));
    }

    // ── GROUP BY / HAVING ────────────────────────────────────────────────────

    @Test
    @DisplayName("GROUP BY single column")
    void testGroupBy() {
        String sql = builder.select("city", "COUNT(*) AS cnt")
                .from("users")
                .groupBy("city")
                .build();
        assertEquals("SELECT city, COUNT(*) AS cnt FROM users GROUP BY city", sql);
    }

    @Test
    @DisplayName("GROUP BY multiple columns")
    void testGroupByMultiple() {
        String sql = builder.select("city", "age")
                .from("users")
                .groupBy("city", "age")
                .build();
        assertTrue(sql.contains("GROUP BY city, age"));
    }

    @Test
    @DisplayName("HAVING clause included after GROUP BY")
    void testHaving() {
        String sql = builder.select("city", "COUNT(*) AS cnt")
                .from("users")
                .groupBy("city")
                .having("COUNT(*) > 5")
                .build();
        assertTrue(sql.contains("HAVING COUNT(*) > 5"));
    }

    @Test
    @DisplayName("HAVING without GROUP BY throws")
    void testHavingWithoutGroupByThrows() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> builder.select("city").from("users").having("COUNT(*) > 1").build());
        assertTrue(ex.getMessage().contains("HAVING"));
    }

    // ── ORDER BY ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ORDER BY ASC")
    void testOrderByAsc() {
        String sql = builder.select("*").from("users").orderBy("name", "ASC").build();
        assertTrue(sql.contains("ORDER BY name ASC"));
    }

    @Test
    @DisplayName("ORDER BY DESC")
    void testOrderByDesc() {
        String sql = builder.select("*").from("users").orderBy("age", "DESC").build();
        assertTrue(sql.contains("ORDER BY age DESC"));
    }

    @Test
    @DisplayName("Invalid direction defaults to ASC")
    void testOrderByInvalidDirection() {
        String sql = builder.select("*").from("users").orderBy("age", "INVALID").build();
        assertTrue(sql.contains("ORDER BY age ASC"));
    }

    @Test
    @DisplayName("Multiple ORDER BY columns appended")
    void testMultipleOrderBy() {
        String sql = builder.select("*").from("users")
                .orderBy("city", "ASC").orderBy("name", "ASC").build();
        assertTrue(sql.contains("ORDER BY city ASC, name ASC"));
    }

    // ── LIMIT / OFFSET ───────────────────────────────────────────────────────

    @Test
    @DisplayName("LIMIT clause")
    void testLimit() {
        String sql = builder.select("*").from("users").limit(10).build();
        assertTrue(sql.endsWith("LIMIT 10"));
    }

    @Test
    @DisplayName("LIMIT + OFFSET pagination")
    void testLimitOffset() {
        String sql = builder.select("*").from("users").limit(10).offset(20).build();
        assertTrue(sql.contains("LIMIT 10 OFFSET 20"));
    }

    // ── FULL QUERY ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Full complex query assembles in correct clause order")
    void testFullQuery() {
        String sql = builder
                .select("u.name", "SUM(o.total_price) AS total")
                .from("users u")
                .innerJoin("orders o", "o.user_id = u.id")
                .where("u.city = ?")
                .groupBy("u.id", "u.name")
                .having("SUM(o.total_price) > 1000")
                .orderBy("total", "DESC")
                .limit(5)
                .build();

        // Verify all clauses present
        assertTrue(sql.startsWith("SELECT u.name"));
        assertTrue(sql.contains("FROM users u"));
        assertTrue(sql.contains("INNER JOIN orders o ON o.user_id = u.id"));
        assertTrue(sql.contains("WHERE u.city = ?"));
        assertTrue(sql.contains("GROUP BY u.id, u.name"));
        assertTrue(sql.contains("HAVING SUM(o.total_price) > 1000"));
        assertTrue(sql.contains("ORDER BY total DESC"));
        assertTrue(sql.endsWith("LIMIT 5"));
    }

    // ── RESET ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("reset() clears all state for reuse")
    void testReset() {
        builder.select("id", "name").from("users").where("id = ?").limit(1);
        builder.reset();

        String sql = builder.select("COUNT(*)").from("orders").build();
        assertEquals("SELECT COUNT(*) FROM orders", sql);
        assertFalse(sql.contains("users"));
        assertFalse(sql.contains("WHERE"));
        assertFalse(sql.contains("LIMIT"));
    }
}
