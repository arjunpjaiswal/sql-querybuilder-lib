# SQL QueryBuilder

A fluent SQL query builder implementing the **Builder Design Pattern**, full **JDBC** integration, **CRUD** operations, **Joins**, **Aggregations**, and **SOLID principles** throughout.

## Project Structure

```
sql-querybuilder/
├── pom.xml                          ← Maven build (Java 17, MySQL connector, JUnit 5)
├── schema.sql                       ← MySQL schema setup — run this first
├── README.md
├── src/main/java/com/querybuilder/
│   ├── core/                        ← Builder layer
│   │   ├── IQueryBuilder.java       ← SELECT builder interface
│   │   ├── IMutationBuilder.java    ← INSERT/UPDATE/DELETE interface
│   │   ├── SelectQueryBuilder.java  ← Concrete SELECT builder
│   │   ├── InsertQueryBuilder.java  ← Concrete INSERT builder
│   │   ├── UpdateQueryBuilder.java  ← Concrete UPDATE builder
│   │   └── DeleteQueryBuilder.java  ← Concrete DELETE builder
│   ├── jdbc/                        ← JDBC layer
│   │   ├── DatabaseConnection.java  ← Singleton connection manager
│   │   └── QueryExecutor.java       ← SQL execution engine (PreparedStatement)
│   ├── model/                       ← Plain Java Objects (POJOs)
│   │   ├── User.java
│   │   ├── Product.java
│   │   └── Order.java
│   ├── dao/                         ← Data Access Object layer
│   │   ├── UserDAO.java
│   │   ├── ProductDAO.java
│   │   └── OrderDAO.java
│   └── Main.java                    ← Full demo entry point
└── src/test/java/com/querybuilder/
    ├── SelectQueryBuilderTest.java  ← 25 tests for SELECT builder
    └── MutationBuilderTest.java     ← 7 tests for INSERT/UPDATE/DELETE builders
```

## Quick Start

### 1. Set up the database
```bash
mysql -u root -p < schema.sql
```

### 2. Build the project
```bash
mvn package
```

### 3. Run unit tests (no database needed)
```bash
mvn test
```

### 4. Run the demo
```bash
java -jar target/sql-querybuilder-1.0.0-jar-with-dependencies.jar
```

If your MySQL password is not set in `DatabaseConnection.java`, override it:
```bash
java -Ddb.password=yourpassword -jar target/sql-querybuilder-1.0.0-jar-with-dependencies.jar
```

## What the demo covers

| # | Section | Concepts |
|---|---------|----------|
| 1 | INSERT users | InsertQueryBuilder, executeInsertGetKey, auto-generated PKs |
| 2 | INSERT products | Same pattern on a different table |
| 3 | SELECT all users | SelectQueryBuilder, ORDER BY |
| 4a | SELECT by id | WHERE with single condition |
| 4b | SELECT by city | WHERE with string parameter |
| 4c | SELECT by age range | WHERE + AND chaining |
| 4d | Search by name | LIKE with % wildcards |
| 5 | UPDATE user | UpdateQueryBuilder, value binding order |
| 6 | INNER JOIN orders | Multi-table query, table aliases |
| 7 | LEFT JOIN order count | All users including those with 0 orders |
| 8 | Triple JOIN | Three tables joined, category + date filter |
| 9 | GROUP BY + HAVING | Top spenders report, aggregation |
| 10 | Pagination | LIMIT + OFFSET |
| 11 | SELECT DISTINCT | Unique city values |
| 12 | Revenue by category | COUNT, SUM, AVG, MAX aggregations |
| 13 | Subquery | Builder composition, nested SELECT |
| 14 | Builder reset and reuse | reset() clears all state |
| 15 | Safety — UPDATE | IllegalStateException without WHERE |
| 16 | Safety — DELETE | IllegalStateException without WHERE |
| 17 | DELETE + count | DeleteQueryBuilder, UserDAO.count() |

## Builder Usage Examples

### SELECT
```java
String sql = new SelectQueryBuilder()
    .select("id", "name", "email")
    .from("users")
    .where("age > ?")
    .and("city = ?")
    .orderBy("name", "ASC")
    .limit(10)
    .build();

List<Map<String, Object>> rows = executor.executeQuery(sql, 18, "Mumbai");
```

### SELECT with JOIN + GROUP BY + HAVING
```java
String sql = new SelectQueryBuilder()
    .select("u.name", "SUM(o.total_price) AS lifetime_value")
    .from("users u")
    .innerJoin("orders o", "o.user_id = u.id")
    .groupBy("u.id", "u.name")
    .having("SUM(o.total_price) > 5000")
    .orderBy("lifetime_value", "DESC")
    .limit(10)
    .build();
```

### INSERT
```java
InsertQueryBuilder qb = new InsertQueryBuilder()
    .into("users")
    .set("name",  "Alice")
    .set("email", "alice@example.com")
    .set("age",   28);

long newId = executor.executeInsertGetKey(qb.build(), qb.getValues());
```

### UPDATE
```java
UpdateQueryBuilder qb = new UpdateQueryBuilder()
    .update("users")
    .set("city", "Delhi")
    .where("id = ?", 1);

int rows = executor.executeUpdate(qb.build(), qb.getValues());
```

### DELETE
```java
DeleteQueryBuilder qb = new DeleteQueryBuilder()
    .from("users")
    .where("id = ?", 5);

int rows = executor.executeUpdate(qb.build(), qb.getValues());
```

### Builder reset and reuse
```java
SelectQueryBuilder b = new SelectQueryBuilder();

String q1 = b.select("name").from("users").where("city = ?").build();
b.reset();

String q2 = b.select("COUNT(*)").from("orders").build();
```

### Using the DAO layer
```java
UserDAO userDAO = new UserDAO();

// Create
long id = userDAO.create(new User("Alice", "alice@x.com", 28, "Mumbai"));

// Read
Map<String, Object> user  = userDAO.findById(1);
List<...>           users = userDAO.findByCity("Mumbai");
List<...>           range = userDAO.findByAgeRange(20, 35);
List<...>           found = userDAO.searchByName("ali");   // LIKE %ali%

// Update
userDAO.update(new User(1, "Alice", "alice@x.com", 29, "Pune"));

// Delete
userDAO.delete(1);
```

## Unit Tests

32 tests verify all builder logic without requiring a database connection.

```bash
mvn test
```

```
Tests run: 32, Failures: 0, Errors: 0
BUILD SUCCESS
```

Tests cover: SELECT, WHERE, AND, OR, JOIN, GROUP BY, HAVING, ORDER BY, LIMIT, OFFSET,
DISTINCT, reset(), INSERT values order, UPDATE values order, safety guards on UPDATE
and DELETE without WHERE, and IMutationBuilder interface assignability.

## SOLID Principles Applied

| Principle | Where |
|-----------|-------|
| **S** Single Responsibility | Each class has one job — build SQL, execute SQL, manage connection, or handle one entity's CRUD |
| **O** Open/Closed | Add new builders or DAOs without modifying existing ones |
| **L** Liskov Substitution | `SelectQueryBuilder` works anywhere `IQueryBuilder` is expected |
| **I** Interface Segregation | `IQueryBuilder` (SELECT only) is separate from `IMutationBuilder` (CUD only) |
| **D** Dependency Inversion | DAOs depend on builder interfaces, not concrete classes; `QueryExecutor` depends on `java.sql.Connection` interface |

## Security

All values are bound via `PreparedStatement` `?` placeholders — never string-concatenated.
`UpdateQueryBuilder` and `DeleteQueryBuilder` throw `IllegalStateException` if called
without a `WHERE` clause, preventing accidental full-table updates or deletes.

## Tech Stack

- Java 17
- JDBC (java.sql)
- MySQL 8.x
- Maven
- JUnit 5
