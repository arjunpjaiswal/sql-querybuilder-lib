package com.querybuilder;

import com.querybuilder.core.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InsertQueryBuilder, UpdateQueryBuilder, DeleteQueryBuilder.
 * No database required.
 */
@DisplayName("Mutation Builder Tests")
class MutationBuilderTest {

    // ═══════════════════════════════════════════════════════════
    // InsertQueryBuilder
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("InsertQueryBuilder")
    class InsertTests {

        @Test
        @DisplayName("Builds correct INSERT SQL")
        void testInsertSql() {
            InsertQueryBuilder qb = new InsertQueryBuilder()
                    .into("users")
                    .set("name",  "Alice")
                    .set("email", "alice@example.com")
                    .set("age",   28);

            assertEquals(
                "INSERT INTO users (name, email, age) VALUES (?, ?, ?)",
                qb.build());
        }

        @Test
        @DisplayName("getValues() returns values in set() order")
        void testInsertValues() {
            InsertQueryBuilder qb = new InsertQueryBuilder()
                    .into("users")
                    .set("name",  "Alice")
                    .set("email", "alice@example.com")
                    .set("age",   28);

            Object[] vals = qb.getValues();
            assertEquals(3,                    vals.length);
            assertEquals("Alice",              vals[0]);
            assertEquals("alice@example.com",  vals[1]);
            assertEquals(28,                   vals[2]);
        }

        @Test
        @DisplayName("Missing table throws IllegalStateException")
        void testMissingTableThrows() {
            assertThrows(IllegalStateException.class,
                () -> new InsertQueryBuilder().set("name", "X").build());
        }

        @Test
        @DisplayName("Missing columns throws IllegalStateException")
        void testMissingColumnsThrows() {
            assertThrows(IllegalStateException.class,
                () -> new InsertQueryBuilder().into("users").build());
        }

        @Test
        @DisplayName("reset() clears all state")
        void testReset() {
            InsertQueryBuilder qb = new InsertQueryBuilder()
                    .into("users").set("name", "Alice");
            qb.reset();
            assertThrows(IllegalStateException.class, qb::build);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // UpdateQueryBuilder
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("UpdateQueryBuilder")
    class UpdateTests {

        @Test
        @DisplayName("Builds correct UPDATE SQL")
        void testUpdateSql() {
            UpdateQueryBuilder qb = new UpdateQueryBuilder()
                    .update("users")
                    .set("city", "Delhi")
                    .set("age",  30)
                    .where("id = ?", 1);

            assertEquals(
                "UPDATE users SET city = ?, age = ? WHERE id = ?",
                qb.build());
        }

        @Test
        @DisplayName("getValues() returns SET values first, then WHERE values")
        void testUpdateValues() {
            UpdateQueryBuilder qb = new UpdateQueryBuilder()
                    .update("users")
                    .set("city", "Delhi")
                    .set("age",  30)
                    .where("id = ?", 1);

            Object[] vals = qb.getValues();
            assertEquals(3,       vals.length);
            assertEquals("Delhi", vals[0]);   // SET city
            assertEquals(30,      vals[1]);   // SET age
            assertEquals(1,       vals[2]);   // WHERE id
        }

        @Test
        @DisplayName("Multiple WHERE conditions joined with AND")
        void testMultipleWhereConditions() {
            UpdateQueryBuilder qb = new UpdateQueryBuilder()
                    .update("users")
                    .set("city", "Pune")
                    .where("id = ?",   1)
                    .where("age > ?", 18);

            String sql = qb.build();
            assertTrue(sql.contains("WHERE id = ? AND age > ?"));

            Object[] vals = qb.getValues();
            assertEquals("Pune", vals[0]);
            assertEquals(1,      vals[1]);
            assertEquals(18,     vals[2]);
        }

        @Test
        @DisplayName("UPDATE without WHERE throws — safety guard")
        void testMissingWhereThrows() {
            assertThrows(IllegalStateException.class,
                () -> new UpdateQueryBuilder()
                        .update("users")
                        .set("city", "Mars")
                        .build());
        }

        @Test
        @DisplayName("UPDATE without table throws")
        void testMissingTableThrows() {
            assertThrows(IllegalStateException.class,
                () -> new UpdateQueryBuilder()
                        .set("city", "Mars")
                        .where("id = ?", 1)
                        .build());
        }

        @Test
        @DisplayName("UPDATE without SET columns throws")
        void testMissingSetThrows() {
            assertThrows(IllegalStateException.class,
                () -> new UpdateQueryBuilder()
                        .update("users")
                        .where("id = ?", 1)
                        .build());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DeleteQueryBuilder
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DeleteQueryBuilder")
    class DeleteTests {

        @Test
        @DisplayName("Builds correct DELETE SQL")
        void testDeleteSql() {
            DeleteQueryBuilder qb = new DeleteQueryBuilder()
                    .from("users")
                    .where("id = ?", 5);

            assertEquals("DELETE FROM users WHERE id = ?", qb.build());
        }

        @Test
        @DisplayName("getValues() returns WHERE values in order")
        void testDeleteValues() {
            DeleteQueryBuilder qb = new DeleteQueryBuilder()
                    .from("users")
                    .where("id = ?", 5);

            Object[] vals = qb.getValues();
            assertEquals(1, vals.length);
            assertEquals(5, vals[0]);
        }

        @Test
        @DisplayName("Multiple WHERE conditions joined with AND")
        void testMultipleConditions() {
            DeleteQueryBuilder qb = new DeleteQueryBuilder()
                    .from("orders")
                    .where("user_id = ?",  1)
                    .where("status = ?",   "cancelled");

            assertTrue(qb.build().contains("WHERE user_id = ? AND status = ?"));

            Object[] vals = qb.getValues();
            assertEquals(2,           vals.length);
            assertEquals(1,           vals[0]);
            assertEquals("cancelled", vals[1]);
        }

        @Test
        @DisplayName("DELETE without WHERE throws — safety guard")
        void testMissingWhereThrows() {
            assertThrows(IllegalStateException.class,
                () -> new DeleteQueryBuilder().from("users").build());
        }

        @Test
        @DisplayName("DELETE without table throws")
        void testMissingTableThrows() {
            assertThrows(IllegalStateException.class,
                () -> new DeleteQueryBuilder().where("id = ?", 1).build());
        }

        @Test
        @DisplayName("reset() clears all state")
        void testReset() {
            DeleteQueryBuilder qb = new DeleteQueryBuilder()
                    .from("users").where("id = ?", 1);
            qb.reset();
            assertThrows(IllegalStateException.class, qb::build);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // Interface contract tests
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("IMutationBuilder contract")
    class InterfaceContractTests {

        @Test
        @DisplayName("InsertQueryBuilder is assignable to IMutationBuilder")
        void testInsertAssignable() {
            IMutationBuilder qb = new InsertQueryBuilder()
                    .into("users").set("name", "Test");
            assertNotNull(qb.build());
            assertNotNull(qb.getValues());
        }

        @Test
        @DisplayName("UpdateQueryBuilder is assignable to IMutationBuilder")
        void testUpdateAssignable() {
            IMutationBuilder qb = new UpdateQueryBuilder()
                    .update("users").set("name", "Test").where("id = ?", 1);
            assertNotNull(qb.build());
            assertNotNull(qb.getValues());
        }

        @Test
        @DisplayName("DeleteQueryBuilder is assignable to IMutationBuilder")
        void testDeleteAssignable() {
            IMutationBuilder qb = new DeleteQueryBuilder()
                    .from("users").where("id = ?", 1);
            assertNotNull(qb.build());
            assertNotNull(qb.getValues());
        }
    }
}
