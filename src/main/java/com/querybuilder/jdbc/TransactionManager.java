package com.querybuilder.jdbc;

import com.querybuilder.core.InsertQueryBuilder;
import com.querybuilder.core.SelectQueryBuilder;
import com.querybuilder.core.UpdateQueryBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * TransactionManager — wraps multi-step operations in a single ACID transaction.
 *
 * Demonstrates Atomicity: ALL steps succeed together, or ALL are rolled back.
 * Uses our QueryBuilders internally for SQL construction so no raw SQL strings
 * leak into this class.
 *
 * SOLID:
 *   S — Single Responsibility: only manages transaction boundaries.
 *   D — Dependency Inversion: uses builder interfaces for SQL, Connection interface for JDBC.
 */
public class TransactionManager {

    private final Connection    connection;
    private final QueryExecutor executor;

    public TransactionManager() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
        this.executor   = new QueryExecutor();
    }

    /**
     * placeOrder — inserts an order AND reduces product stock atomically.
     *
     * Steps:
     *   1. Read current product stock.
     *   2. Validate sufficient stock is available.
     *   3. INSERT the order row.
     *   4. UPDATE the product stock.
     *   5. COMMIT — only if all four steps succeed.
     *
     * If ANY step fails, ROLLBACK undoes everything — the database remains
     * consistent as if the method was never called.
     *
     * @param userId    the buyer's user id
     * @param productId the product being ordered
     * @param qty       quantity ordered
     * @param unitPrice price per unit
     * @return true if the order was placed successfully
     */
    public boolean placeOrder(int userId, int productId, int qty, double unitPrice)
            throws SQLException {

        connection.setAutoCommit(false);   // BEGIN TRANSACTION
        try {
            // Step 1: Read current stock ────────────────────────────────────
            String checkSql = new SelectQueryBuilder()
                    .select("stock")
                    .from("products")
                    .where("id = ?")
                    .build();

            List<Map<String, Object>> res = executor.executeQuery(checkSql, productId);
            if (res.isEmpty())
                throw new SQLException("Product not found: id=" + productId);

            int stock = ((Number) res.get(0).get("stock")).intValue();

            // Step 2: Validate stock ─────────────────────────────────────────
            if (stock < qty)
                throw new SQLException(
                    "Insufficient stock for product " + productId +
                    ": have " + stock + ", need " + qty);

            // Step 3: Insert the order ───────────────────────────────────────
            InsertQueryBuilder orderInsert = new InsertQueryBuilder()
                    .into("orders")
                    .set("user_id",     userId)
                    .set("product_id",  productId)
                    .set("quantity",    qty)
                    .set("total_price", unitPrice * qty);

            long orderId = executor.executeInsertGetKey(
                orderInsert.build(), orderInsert.getValues());
            System.out.println("[TXN] Order row created, id=" + orderId);

            // Step 4: Reduce stock ───────────────────────────────────────────
            UpdateQueryBuilder stockUpdate = new UpdateQueryBuilder()
                    .update("products")
                    .set("stock", stock - qty)
                    .where("id = ?", productId);

            executor.executeUpdate(stockUpdate.build(), stockUpdate.getValues());
            System.out.println("[TXN] Stock reduced to " + (stock - qty));

            // Step 5: Commit — make all changes permanent ────────────────────
            connection.commit();
            System.out.println("[TXN] Order placed successfully. Order ID=" + orderId);
            return true;

        } catch (SQLException e) {
            connection.rollback();   // undo ALL changes on any failure
            System.err.println("[TXN] Rolled back: " + e.getMessage());
            return false;

        } finally {
            connection.setAutoCommit(true);  // always restore default for next ops
        }
    }

    /**
     * transferStock — moves qty units from one product to another atomically.
     * Demonstrates a pure UPDATE-only transaction.
     */
    public boolean transferStock(int fromProductId, int toProductId, int qty)
            throws SQLException {

        connection.setAutoCommit(false);
        try {
            // Reduce from-product stock
            String deductSql = new UpdateQueryBuilder()
                    .update("products")
                    .set("stock", qty)          // placeholder — see note below
                    .where("id = ?", fromProductId)
                    .build();
            // Use raw SQL expression for stock arithmetic to keep it atomic
            executor.executeUpdate(
                "UPDATE products SET stock = stock - ? WHERE id = ?",
                qty, fromProductId);

            // Increase to-product stock
            executor.executeUpdate(
                "UPDATE products SET stock = stock + ? WHERE id = ?",
                qty, toProductId);

            connection.commit();
            System.out.println("[TXN] Transferred " + qty + " units from product "
                + fromProductId + " to " + toProductId);
            return true;

        } catch (SQLException e) {
            connection.rollback();
            System.err.println("[TXN] Transfer rolled back: " + e.getMessage());
            return false;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
