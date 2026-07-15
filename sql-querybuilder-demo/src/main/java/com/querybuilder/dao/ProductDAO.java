package com.querybuilder.dao;

import com.querybuilder.core.*;
import com.querybuilder.jdbc.QueryExecutor;
import com.querybuilder.model.Product;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * ProductDAO — all database operations for the Product entity.
 *
 * SOLID:
 *   S — Single Responsibility: CRUD for Product only.
 *   D — Dependency Inversion: uses IQueryBuilder and IMutationBuilder interfaces.
 */
public class ProductDAO {

    private final QueryExecutor executor;

    public ProductDAO() throws SQLException {
        this.executor = new QueryExecutor();
    }

    // ── CREATE ───────────────────────────────────────────────────────────────
    public long create(Product product) throws SQLException {
        IMutationBuilder qb = new InsertQueryBuilder()
                .into("products")
                .set("name",     product.getName())
                .set("price",    product.getPrice())
                .set("category", product.getCategory())
                .set("stock",    product.getStock());
        return executor.executeInsertGetKey(qb.build(), qb.getValues());
    }

    // ── READ ALL ─────────────────────────────────────────────────────────────
    public List<Map<String, Object>> findAll() throws SQLException {
        IQueryBuilder qb = new SelectQueryBuilder()
                .select("*")
                .from("products")
                .orderBy("name", "ASC");
        return executor.executeQuery(qb.build());
    }

    // ── READ BY ID ───────────────────────────────────────────────────────────
    public Map<String, Object> findById(int id) throws SQLException {
        IQueryBuilder qb = new SelectQueryBuilder()
                .select("*")
                .from("products")
                .where("id = ?");
        List<Map<String, Object>> rows = executor.executeQuery(qb.build(), id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    // ── READ BY CATEGORY ─────────────────────────────────────────────────────
    public List<Map<String, Object>> findByCategory(String category) throws SQLException {
        IQueryBuilder qb = new SelectQueryBuilder()
                .select("id", "name", "price", "stock")
                .from("products")
                .where("category = ?")
                .orderBy("price", "ASC");
        return executor.executeQuery(qb.build(), category);
    }

    // ── READ IN STOCK ─────────────────────────────────────────────────────────
    /**
     * Returns all products that currently have stock > 0.
     */
    public List<Map<String, Object>> findInStock() throws SQLException {
        IQueryBuilder qb = new SelectQueryBuilder()
                .select("id", "name", "category", "price", "stock")
                .from("products")
                .where("stock > ?")
                .orderBy("category", "ASC")
                .orderBy("name", "ASC");
        return executor.executeQuery(qb.build(), 0);
    }

    // ── READ BY PRICE RANGE ───────────────────────────────────────────────────
    public List<Map<String, Object>> findByPriceRange(double minPrice, double maxPrice)
            throws SQLException {
        IQueryBuilder qb = new SelectQueryBuilder()
                .select("id", "name", "category", "price", "stock")
                .from("products")
                .where("price >= ?")
                .and("price <= ?")
                .orderBy("price", "ASC");
        return executor.executeQuery(qb.build(), minPrice, maxPrice);
    }

    // ── REVENUE REPORT BY CATEGORY ────────────────────────────────────────────
    /**
     * Returns aggregated sales data grouped by category.
     * Demonstrates GROUP BY + aggregate functions via the builder.
     */
    public List<Map<String, Object>> revenueByCategory() throws SQLException {
        IQueryBuilder qb = new SelectQueryBuilder()
                .select(
                    "p.category",
                    "COUNT(o.id)        AS total_orders",
                    "SUM(o.total_price) AS total_revenue",
                    "AVG(o.total_price) AS avg_order_value",
                    "MAX(o.total_price) AS largest_order"
                )
                .from("orders o")
                .innerJoin("products p", "p.id = o.product_id")
                .groupBy("p.category")
                .orderBy("total_revenue", "DESC");
        return executor.executeQuery(qb.build());
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    public int update(Product product) throws SQLException {
        IMutationBuilder qb = new UpdateQueryBuilder()
                .update("products")
                .set("name",     product.getName())
                .set("price",    product.getPrice())
                .set("category", product.getCategory())
                .set("stock",    product.getStock())
                .where("id = ?", product.getId());
        return executor.executeUpdate(qb.build(), qb.getValues());
    }

    // ── UPDATE STOCK ──────────────────────────────────────────────────────────
    public int updateStock(int productId, int newStock) throws SQLException {
        IMutationBuilder qb = new UpdateQueryBuilder()
                .update("products")
                .set("stock", newStock)
                .where("id = ?", productId);
        return executor.executeUpdate(qb.build(), qb.getValues());
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    public int delete(int id) throws SQLException {
        IMutationBuilder qb = new DeleteQueryBuilder()
                .from("products")
                .where("id = ?", id);
        return executor.executeUpdate(qb.build(), qb.getValues());
    }
}
