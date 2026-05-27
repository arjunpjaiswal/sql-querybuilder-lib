package com.querybuilder.dao;

import com.querybuilder.core.*;
import com.querybuilder.jdbc.QueryExecutor;
import com.querybuilder.model.Order;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * OrderDAO — all database operations for the Order entity.
 *
 * Demonstrates multi-table JOINs and aggregation queries through the builder.
 *
 * SOLID:
 *   S — Single Responsibility: CRUD + reporting for Order only.
 *   D — Dependency Inversion: uses IQueryBuilder and IMutationBuilder interfaces.
 */
public class OrderDAO {

    private final QueryExecutor executor;

    public OrderDAO() throws SQLException {
        this.executor = new QueryExecutor();
    }

    // ── CREATE ───────────────────────────────────────────────────────────────
    public long create(Order order) throws SQLException {
        IMutationBuilder qb = new InsertQueryBuilder()
                .into("orders")
                .set("user_id",     order.getUserId())
                .set("product_id",  order.getProductId())
                .set("quantity",    order.getQuantity())
                .set("total_price", order.getTotalPrice());
        return executor.executeInsertGetKey(qb.build(), qb.getValues());
    }

    // ── READ ALL (with JOIN) ──────────────────────────────────────────────────
    /**
     * Returns all orders enriched with customer name and product name.
     * Demonstrates INNER JOIN across three tables.
     */
    public List<Map<String, Object>> findAllWithDetails() throws SQLException {
        IQueryBuilder qb = new SelectQueryBuilder()
                .select(
                    "o.id         AS order_id",
                    "u.name       AS customer",
                    "p.name       AS product",
                    "p.category",
                    "o.quantity",
                    "o.total_price",
                    "o.order_date"
                )
                .from("orders o")
                .innerJoin("users u",    "u.id = o.user_id")
                .innerJoin("products p", "p.id = o.product_id")
                .orderBy("o.order_date", "DESC");
        return executor.executeQuery(qb.build());
    }

    // ── READ BY USER ──────────────────────────────────────────────────────────
    public List<Map<String, Object>> findByUser(int userId) throws SQLException {
        IQueryBuilder qb = new SelectQueryBuilder()
                .select("o.id", "p.name AS product", "o.quantity", "o.total_price", "o.order_date")
                .from("orders o")
                .innerJoin("products p", "p.id = o.product_id")
                .where("o.user_id = ?")
                .orderBy("o.order_date", "DESC");
        return executor.executeQuery(qb.build(), userId);
    }

    // ── ORDERS ABOVE AMOUNT ───────────────────────────────────────────────────
    /**
     * Returns orders with total_price above the given threshold.
     * Demonstrates WHERE with a parameter and ORDER BY.
     */
    public List<Map<String, Object>> findAboveAmount(double minAmount) throws SQLException {
        IQueryBuilder qb = new SelectQueryBuilder()
                .select("u.name AS customer", "o.total_price", "o.order_date")
                .from("users u")
                .innerJoin("orders o", "o.user_id = u.id")
                .where("o.total_price > ?")
                .orderBy("o.order_date", "DESC");
        return executor.executeQuery(qb.build(), minAmount);
    }

    // ── TOP SPENDERS ─────────────────────────────────────────────────────────
    /**
     * Returns users whose total spend exceeds the given threshold.
     * Demonstrates GROUP BY + HAVING.
     */
    public List<Map<String, Object>> findTopSpenders(double minLifetimeValue, int limit)
            throws SQLException {
        IQueryBuilder qb = new SelectQueryBuilder()
                .select(
                    "u.name",
                    "u.email",
                    "COUNT(o.id)        AS total_orders",
                    "SUM(o.total_price) AS lifetime_value"
                )
                .from("users u")
                .innerJoin("orders o", "o.user_id = u.id")
                .groupBy("u.id", "u.name", "u.email")
                .having("SUM(o.total_price) > " + minLifetimeValue)
                .orderBy("lifetime_value", "DESC")
                .limit(limit);
        return executor.executeQuery(qb.build());
    }

    // ── ORDER COUNT PER USER (LEFT JOIN) ──────────────────────────────────────
    /**
     * Returns ALL users with their order count — users with 0 orders are included.
     * Demonstrates LEFT JOIN + GROUP BY.
     */
    public List<Map<String, Object>> orderCountPerUser() throws SQLException {
        IQueryBuilder qb = new SelectQueryBuilder()
                .select("u.name", "u.city", "COUNT(o.id) AS order_count")
                .from("users u")
                .leftJoin("orders o", "o.user_id = u.id")
                .groupBy("u.id", "u.name", "u.city")
                .orderBy("order_count", "DESC");
        return executor.executeQuery(qb.build());
    }

    // ── ORDERS BY CATEGORY + DATE RANGE ──────────────────────────────────────
    /**
     * Returns detailed orders filtered by product category and date range.
     * Demonstrates triple JOIN + multiple WHERE conditions + LIMIT.
     */
    public List<Map<String, Object>> findByCategoryAndDate(
            String category, String fromDate, int limit) throws SQLException {
        IQueryBuilder qb = new SelectQueryBuilder()
                .select(
                    "u.name   AS customer",
                    "p.name   AS product",
                    "p.category",
                    "o.quantity",
                    "o.total_price",
                    "o.order_date"
                )
                .from("users u")
                .innerJoin("orders o",   "o.user_id    = u.id")
                .innerJoin("products p", "p.id         = o.product_id")
                .where("p.category = ?")
                .and("o.order_date >= ?")
                .orderBy("o.total_price", "DESC")
                .limit(limit);
        return executor.executeQuery(qb.build(), category, fromDate);
    }

    // ── PAGINATED ORDERS ──────────────────────────────────────────────────────
    /**
     * Returns a single page of orders (LIMIT + OFFSET pagination).
     */
    public List<Map<String, Object>> findPage(int page, int pageSize) throws SQLException {
        int offset = (page - 1) * pageSize;
        IQueryBuilder qb = new SelectQueryBuilder()
                .select("o.id", "u.name AS customer", "o.total_price", "o.order_date")
                .from("orders o")
                .innerJoin("users u", "u.id = o.user_id")
                .orderBy("o.order_date", "DESC")
                .limit(pageSize)
                .offset(offset);
        return executor.executeQuery(qb.build());
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    public int delete(int id) throws SQLException {
        IMutationBuilder qb = new DeleteQueryBuilder()
                .from("orders")
                .where("id = ?", id);
        return executor.executeUpdate(qb.build(), qb.getValues());
    }
}
