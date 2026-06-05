package com.querybuilder;

import com.querybuilder.core.*;
import com.querybuilder.dao.*;
import com.querybuilder.jdbc.*;
import com.querybuilder.model.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws Exception {

        QueryExecutor      executor   = new QueryExecutor();
        UserDAO            userDAO    = new UserDAO();
        ProductDAO         productDAO = new ProductDAO();
        OrderDAO           orderDAO   = new OrderDAO();
        TransactionManager txn        = new TransactionManager();

        // ── Clean slate before every run ─────────────────────────────────────
        executor.executeUpdate("DELETE FROM orders");
        executor.executeUpdate("DELETE FROM users");
        executor.executeUpdate("DELETE FROM products");
        executor.executeUpdate("ALTER TABLE orders   AUTO_INCREMENT = 1");
        executor.executeUpdate("ALTER TABLE users    AUTO_INCREMENT = 1");
        executor.executeUpdate("ALTER TABLE products AUTO_INCREMENT = 1");
        System.out.println("[INIT] Tables cleared. Starting fresh.");

        // ════════════════════════════════════════════════════════════════════
        separator("1. INSERT USERS");
        // ════════════════════════════════════════════════════════════════════

        User alice = new User("Alice Johnson", "alice@example.com", 28, "Mumbai");
        User bob   = new User("Bob Smith",     "bob@example.com",   35, "Delhi");
        User carol = new User("Carol Lee",     "carol@example.com", 24, "Mumbai");
        User dave  = new User("Dave Kumar",    "dave@example.com",  42, "Pune");

        long aliceId = userDAO.create(alice);  alice.setId((int) aliceId);
        long bobId   = userDAO.create(bob);    bob.setId((int) bobId);
        long carolId = userDAO.create(carol);  carol.setId((int) carolId);
        long daveId  = userDAO.create(dave);   dave.setId((int) daveId);

        System.out.printf("  Created: Alice=%d, Bob=%d, Carol=%d, Dave=%d%n",
                aliceId, bobId, carolId, daveId);

        // ════════════════════════════════════════════════════════════════════
        separator("2. INSERT PRODUCTS");
        // ════════════════════════════════════════════════════════════════════

        Product laptop = new Product("Laptop Pro",      new BigDecimal("79999.00"), "Electronics", 50);
        Product phone  = new Product("Smartphone X",    new BigDecimal("24999.00"), "Electronics", 100);
        Product desk   = new Product("Standing Desk",   new BigDecimal("14999.00"), "Furniture",   20);
        Product chair  = new Product("Ergonomic Chair", new BigDecimal("8999.00"),  "Furniture",   30);
        Product book   = new Product("Clean Code",      new BigDecimal("799.00"),   "Books",       200);

        long laptopId = productDAO.create(laptop);  laptop.setId((int) laptopId);
        long phoneId  = productDAO.create(phone);   phone.setId((int) phoneId);
        long deskId   = productDAO.create(desk);    desk.setId((int) deskId);
        long chairId  = productDAO.create(chair);   chair.setId((int) chairId);
        long bookId   = productDAO.create(book);    book.setId((int) bookId);

        System.out.printf("  Created: Laptop=%d, Phone=%d, Desk=%d, Chair=%d, Book=%d%n",
                laptopId, phoneId, deskId, chairId, bookId);

        // ════════════════════════════════════════════════════════════════════
        separator("3. SELECT ALL USERS");
        // ════════════════════════════════════════════════════════════════════

        userDAO.findAll().forEach(r -> System.out.println("  " + r));

        // ════════════════════════════════════════════════════════════════════
        separator("4a. SELECT BY ID");
        // ════════════════════════════════════════════════════════════════════

        Map<String, Object> found = userDAO.findById((int) aliceId);
        System.out.println("  findById(" + aliceId + "): " + found);

        // ════════════════════════════════════════════════════════════════════
        separator("4b. SELECT BY CITY");
        // ════════════════════════════════════════════════════════════════════

        userDAO.findByCity("Mumbai").forEach(r -> System.out.println("  " + r));

        // ════════════════════════════════════════════════════════════════════
        separator("4c. SELECT BY AGE RANGE (25-40)");
        // ════════════════════════════════════════════════════════════════════

        userDAO.findByAgeRange(25, 40).forEach(r -> System.out.println("  " + r));

        // ════════════════════════════════════════════════════════════════════
        separator("4d. SEARCH BY NAME LIKE 'a'");
        // ════════════════════════════════════════════════════════════════════

        userDAO.searchByName("a").forEach(r -> System.out.println("  " + r));

        // ════════════════════════════════════════════════════════════════════
        separator("5. UPDATE USER");
        // ════════════════════════════════════════════════════════════════════

        alice.setCity("Pune");
        alice.setAge(29);
        int updated = userDAO.update(alice);
        System.out.println("  Rows updated: " + updated);
        System.out.println("  After update: " + userDAO.findById((int) aliceId));

        // ════════════════════════════════════════════════════════════════════
        separator("6. PLACE ORDERS via TRANSACTION");
        // ════════════════════════════════════════════════════════════════════

        boolean o1 = txn.placeOrder((int) aliceId, (int) laptopId, 1, 79999.00);
        boolean o2 = txn.placeOrder((int) bobId,   (int) phoneId,  2, 24999.00);
        boolean o3 = txn.placeOrder((int) carolId, (int) deskId,   1, 14999.00);
        boolean o4 = txn.placeOrder((int) daveId,  (int) bookId,   3, 799.00);
        boolean o5 = txn.placeOrder((int) aliceId, (int) chairId,  1, 8999.00);
        System.out.printf("  Orders placed: o1=%b o2=%b o3=%b o4=%b o5=%b%n",
                o1, o2, o3, o4, o5);

        // ════════════════════════════════════════════════════════════════════
        separator("7. INNER JOIN — ALL ORDERS WITH DETAILS");
        // ════════════════════════════════════════════════════════════════════

        orderDAO.findAllWithDetails().forEach(r -> System.out.println("  " + r));

        // ════════════════════════════════════════════════════════════════════
        separator("8. LEFT JOIN — ORDER COUNT PER USER (including 0-order users)");
        // ════════════════════════════════════════════════════════════════════

        orderDAO.orderCountPerUser().forEach(r -> System.out.println("  " + r));

        // ════════════════════════════════════════════════════════════════════
        separator("9. TRIPLE JOIN — Electronics orders");
        // ════════════════════════════════════════════════════════════════════

        orderDAO.findByCategoryAndDate("Electronics", "2020-01-01", 10)
                .forEach(r -> System.out.println("  " + r));

        // ════════════════════════════════════════════════════════════════════
        separator("10. GROUP BY + HAVING — TOP SPENDERS (lifetime > 5000)");
        // ════════════════════════════════════════════════════════════════════

        orderDAO.findTopSpenders(5000.0, 5)
                .forEach(r -> System.out.println("  " + r));

        // ════════════════════════════════════════════════════════════════════
        separator("11. PAGINATION — page 1 (pageSize=3)");
        // ════════════════════════════════════════════════════════════════════

        orderDAO.findPage(1, 3).forEach(r -> System.out.println("  " + r));

        // ════════════════════════════════════════════════════════════════════
        separator("12. SELECT DISTINCT CITIES");
        // ════════════════════════════════════════════════════════════════════

        userDAO.findDistinctCities().forEach(r -> System.out.println("  " + r));

        // ════════════════════════════════════════════════════════════════════
        separator("13. REVENUE BY CATEGORY (aggregation)");
        // ════════════════════════════════════════════════════════════════════

        productDAO.revenueByCategory().forEach(r -> System.out.println("  " + r));

        // ════════════════════════════════════════════════════════════════════
        separator("14. SUBQUERY — users above-average spenders");
        // ════════════════════════════════════════════════════════════════════

        String subquery = new SelectQueryBuilder()
                .select("user_id")
                .from("orders")
                .groupBy("user_id")
                .having("AVG(total_price) > (SELECT AVG(total_price) FROM orders)")
                .build();

        String outerSql = new SelectQueryBuilder()
                .select("id", "name", "email")
                .from("users")
                .where("id IN (" + subquery + ")")
                .build();

        System.out.println("  [SQL] " + outerSql);
        executor.executeQuery(outerSql).forEach(r -> System.out.println("  " + r));

        // ════════════════════════════════════════════════════════════════════
        separator("15. BUILDER RESET AND REUSE");
        // ════════════════════════════════════════════════════════════════════

        SelectQueryBuilder reusable = new SelectQueryBuilder();

        String q1 = reusable.select("name", "email")
                .from("users")
                .where("city = ?")
                .build();
        System.out.println("  q1: " + q1);
        reusable.reset();

        String q2 = reusable.select("name", "price")
                .from("products")
                .where("price < ?")
                .orderBy("price", "ASC")
                .build();
        System.out.println("  q2: " + q2);
        reusable.reset();

        String q3 = reusable.select("COUNT(*) AS total")
                .from("orders")
                .build();
        System.out.println("  q3: " + q3);

        // ════════════════════════════════════════════════════════════════════
        separator("16. SAFETY — UPDATE without WHERE (should throw)");
        // ════════════════════════════════════════════════════════════════════

        try {
            new UpdateQueryBuilder()
                    .update("users")
                    .set("city", "Mars")
                    .build();
            System.out.println("  ERROR: should have thrown!");
        } catch (IllegalStateException e) {
            System.out.println("  Caught expected: " + e.getMessage());
        }

        // ════════════════════════════════════════════════════════════════════
        separator("17. SAFETY — DELETE without WHERE (should throw)");
        // ════════════════════════════════════════════════════════════════════

        try {
            new DeleteQueryBuilder()
                    .from("users")
                    .build();
            System.out.println("  ERROR: should have thrown!");
        } catch (IllegalStateException e) {
            System.out.println("  Caught expected: " + e.getMessage());
        }

        // ════════════════════════════════════════════════════════════════════
        separator("18. DELETE TEST DATA");
        // ════════════════════════════════════════════════════════════════════

        System.out.println("  Deleted Bob: "
                + userDAO.delete((int) bobId) + " row(s)");
        System.out.println("  Total users remaining: " + userDAO.count());

        // ════════════════════════════════════════════════════════════════════
        separator("DONE");
        // ════════════════════════════════════════════════════════════════════

        DatabaseConnection.getInstance().close();
    }

    private static void separator(String title) {
        System.out.println("\n════════════════════════════════════════════════════");
        System.out.println("  " + title);
        System.out.println("════════════════════════════════════════════════════");
    }
}