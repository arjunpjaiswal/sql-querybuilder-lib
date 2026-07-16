# SQL QueryBuilder

[![](https://jitpack.io/v/arjunpjaiswal/sql-querybuilder-lib.svg)](https://jitpack.io/#arjunpjaiswal/sql-querybuilder-lib)

A fluent SQL query builder implementing the **Builder Design Pattern**, full **JDBC** integration, **CRUD** operations, **Joins**, **Aggregations**, **Transactions**, and **SOLID principles** throughout.

## Project Structure

Two Maven modules under a parent aggregator: `core` is the published library, `demo` is a runnable e-commerce example that consumes `core` the same way an external project would.

```
sql-querybuilder/
├── pom.xml                              ← Parent aggregator (packaging=pom)
├── sql-querybuilder-core/               ← THE LIBRARY — this is what you depend on
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/querybuilder/
│       │   ├── core/                    ← Builder layer
│       │   │   ├── IQueryBuilder.java       ← SELECT builder interface
│       │   │   ├── IMutationBuilder.java    ← INSERT/UPDATE/DELETE interface
│       │   │   ├── SelectQueryBuilder.java  ← Concrete SELECT builder
│       │   │   ├── InsertQueryBuilder.java  ← Concrete INSERT builder
│       │   │   ├── UpdateQueryBuilder.java  ← Concrete UPDATE builder
│       │   │   └── DeleteQueryBuilder.java  ← Concrete DELETE builder
│       │   └── jdbc/                    ← JDBC layer
│       │       ├── DatabaseConnection.java  ← Singleton connection manager (config via system properties)
│       │       ├── QueryExecutor.java       ← SQL execution engine (PreparedStatement)
│       │       └── TransactionManager.java  ← ACID transaction handling
│       └── test/java/com/querybuilder/
│           ├── SelectQueryBuilderTest.java  ← 23 tests for SELECT builder
│           └── MutationBuilderTest.java     ← 20 tests for INSERT/UPDATE/DELETE builders
└── sql-querybuilder-demo/               ← Usage example (not published)
    ├── pom.xml                          ← depends on sql-querybuilder-core
    ├── schema.sql                       ← MySQL schema setup — run this first
    └── src/main/java/com/querybuilder/
        ├── model/                       ← Plain Java Objects (POJOs)
        │   ├── User.java
        │   ├── Product.java
        │   └── Order.java
        ├── dao/                         ← Data Access Object layer
        │   ├── UserDAO.java
        │   ├── ProductDAO.java
        │   └── OrderDAO.java
        └── Main.java                    ← Full demo entry point (18 sections)
```

> This is the packaged, JitPack-installable version of the SQL QueryBuilder project — built to be dropped into any Java project as a real dependency, not just cloned and read.

## Installation

Add `sql-querybuilder-core` to your own project via [JitPack](https://jitpack.io).

**Maven**
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.arjunpjaiswal.sql-querybuilder-lib</groupId>
    <artifactId>sql-querybuilder-core</artifactId>
    <version>1.0.1</version>
</dependency>
```

**Gradle**
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.arjunpjaiswal.sql-querybuilder-lib:sql-querybuilder-core:1.0.1'
}
```

The MySQL JDBC driver comes along transitively — no need to declare it separately.

> ⚠️ **Use 1.0.1 or later.** Version `1.0.0` shipped with a hardcoded default database password in `DatabaseConnection.java` and should not be used. `1.0.1` fixed this — all credentials are now supplied by the consumer, never baked into the library.

## Configuration — pointing this at YOUR database

`DatabaseConnection` reads its connection details from **JVM system properties**, so you never edit library source to use your own database — just pass three flags when you run your app:

| Property | Purpose | Default if not set |
|---|---|---|
| `db.url` | Your JDBC connection string | `jdbc:mysql://localhost:3306/querybuilder_db` |
| `db.user` | Your MySQL username | `root` |
| `db.password` | Your MySQL password | *(empty)* |

**Example — running your own app that depends on this library:**

macOS / Linux / Windows CMD:
```bash
java -Ddb.url=jdbc:mysql://localhost:3306/your_db -Ddb.user=root -Ddb.password=yourpassword -cp your-classpath your.MainClass
```

**Windows PowerShell** — wrap each `-D` flag in quotes, or PowerShell mis-parses them:
```powershell
java "-Ddb.url=jdbc:mysql://localhost:3306/your_db" "-Ddb.user=root" "-Ddb.password=yourpassword" -cp your-classpath your.MainClass
```

**In an IDE (IntelliJ, Eclipse, etc.):** set the same flags under your Run Configuration's **VM options** field instead of the command line.

This works identically regardless of build tool (Maven or Gradle) or IDE — it's a plain JVM argument, not tied to any specific tool.

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

### Transaction
```java
TransactionManager txn = new TransactionManager();

// Atomically: checks stock → inserts order → reduces stock
// On ANY failure: auto-rollback, returns false
boolean success = txn.placeOrder(userId, productId, qty, unitPrice);
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

---

## Running the bundled demo locally

The `demo` module is a full e-commerce example showing every feature end-to-end against a real MySQL database.

### 1. Set up the database
```bash
mysql -u root -p < sql-querybuilder-demo/schema.sql
```

### 2. Build everything (core + demo)
```bash
mvn clean install
```

### 3. Run unit tests (no database needed)
```bash
mvn test -pl sql-querybuilder-core
```

### 4. Run the demo
```bash
java -jar sql-querybuilder-demo/target/sql-querybuilder-demo-1.0.1-jar-with-dependencies.jar
```

If your MySQL password differs from the default, pass it via system property (see [Configuration](#configuration--pointing-this-at-your-database) above):

```bash
java -Ddb.password=yourpassword -jar sql-querybuilder-demo/target/sql-querybuilder-demo-1.0.1-jar-with-dependencies.jar
```

Note: The demo clears all three tables and resets AUTO_INCREMENT at startup so it can be run multiple times without duplicate-key errors. Every run starts completely fresh.

### What the demo covers

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
| 6 | Place orders via Transaction | ACID — stock check, INSERT order, UPDATE stock, COMMIT/ROLLBACK |
| 7 | INNER JOIN orders | Multi-table query with table aliases |
| 8 | LEFT JOIN order count | All users including those with 0 orders |
| 9 | Triple JOIN | Three tables joined, category + date filter |
| 10 | GROUP BY + HAVING | Top spenders report, aggregation |
| 11 | Pagination | LIMIT + OFFSET |
| 12 | SELECT DISTINCT | Unique city values |
| 13 | Revenue by category | COUNT, SUM, AVG, MAX aggregations |
| 14 | Subquery | Builder composition, nested SELECT |
| 15 | Builder reset and reuse | reset() clears all state |
| 16 | Safety — UPDATE | IllegalStateException without WHERE |
| 17 | Safety — DELETE | IllegalStateException without WHERE |
| 18 | DELETE + count | DeleteQueryBuilder, UserDAO.count() |

---

## Unit Tests

43 tests verify all builder logic without requiring a database connection.

```bash
mvn test
```

```
Tests run: 43, Failures: 0, Errors: 0
BUILD SUCCESS
```

Tests cover: SELECT, WHERE, AND, OR, JOIN, GROUP BY, HAVING, ORDER BY, LIMIT, OFFSET, DISTINCT, reset(), INSERT values order, UPDATE values order, safety guards on UPDATE and DELETE without WHERE, and IMutationBuilder interface assignability.

`MutationBuilderTest` uses JUnit 5 `@Nested` classes to group tests by builder: `InsertTests` (5), `UpdateTests` (6), `DeleteTests` (6), `InterfaceContractTests` (3).

## Security

- All values are bound via `PreparedStatement` `?` placeholders — never string-concatenated.
- `UpdateQueryBuilder` and `DeleteQueryBuilder` throw `IllegalStateException` if called without a `WHERE` clause, preventing accidental full-table updates or deletes.
- Database credentials are never hardcoded — they're supplied by the consumer via JVM system properties at runtime (see [Configuration](#configuration--pointing-this-at-your-database)).

## SOLID Principles Applied

| Principle | Where |
|-----------|-------|
| **S** Single Responsibility | Each class has one job — build SQL, execute SQL, manage connection, handle transactions, or handle one entity's CRUD |
| **O** Open/Closed | Add new builders or DAOs without modifying existing ones |
| **L** Liskov Substitution | `SelectQueryBuilder` works anywhere `IQueryBuilder` is expected |
| **I** Interface Segregation | `IQueryBuilder` (SELECT only) is separate from `IMutationBuilder` (CUD only) |
| **D** Dependency Inversion | DAOs depend on builder interfaces, not concrete classes; `QueryExecutor` depends on `java.sql.Connection` interface |

## Tech Stack

- Java 17
- JDBC (java.sql)
- MySQL 8.x
- Maven
- JUnit 5
