package com.querybuilder.jdbc;

import com.querybuilder.core.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class TransactionManager {

    private final Connection    connection;
    private final QueryExecutor executor;

    public TransactionManager() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
        this.executor   = new QueryExecutor();
    }

    public boolean placeOrder(int userId, int productId,
                              int qty, double unitPrice)
            throws SQLException {

        connection.setAutoCommit(false);
        try {
            // Step 1 — read current stock
            String checkSql = new SelectQueryBuilder()
                    .select("stock")
                    .from("products")
                    .where("id = ?")
                    .build();

            List<Map<String, Object>> res =
                    executor.executeQuery(checkSql, productId);

            if (res.isEmpty())
                throw new SQLException(
                        "Product not found: id=" + productId);

            int stock = ((Number) res.get(0).get("stock")).intValue();

            // Step 2 — validate stock
            if (stock < qty)
                throw new SQLException(
                        "Insufficient stock. Have: " + stock
                                + ", Need: " + qty);

            // Step 3 — insert the order
            InsertQueryBuilder orderInsert = new InsertQueryBuilder()
                    .into("orders")
                    .set("user_id",     userId)
                    .set("product_id",  productId)
                    .set("quantity",    qty)
                    .set("total_price", unitPrice * qty);

            long orderId = executor.executeInsertGetKey(
                    orderInsert.build(), orderInsert.getValues());
            System.out.println("[TXN] Order created, id=" + orderId);

            // Step 4 — reduce stock
            UpdateQueryBuilder stockUpdate = new UpdateQueryBuilder()
                    .update("products")
                    .set("stock", stock - qty)
                    .where("id = ?", productId);

            executor.executeUpdate(
                    stockUpdate.build(), stockUpdate.getValues());
            System.out.println("[TXN] Stock reduced to "
                    + (stock - qty));

            // Step 5 — commit everything
            connection.commit();
            System.out.println("[TXN] Order placed. ID=" + orderId);
            return true;

        } catch (SQLException e) {
            connection.rollback();
            System.err.println("[TXN] Rolled back: " + e.getMessage());
            return false;

        } finally {
            connection.setAutoCommit(true);
        }
    }
}