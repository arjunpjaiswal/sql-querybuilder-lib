package com.querybuilder.dao;

import com.querybuilder.core.*;
import com.querybuilder.jdbc.QueryExecutor;
import com.querybuilder.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * UserDAO — all database operations for the User entity.
 *
 * The DAO layer completely hides SQL from callers. The service layer
 * just calls create(user) or findByCity("Mumbai") without ever seeing
 * a SQL string.
 *
 * SOLID:
 *   S — Single Responsibility: CRUD for User only, nothing else.
 *   D — Dependency Inversion: uses IQueryBuilder and IMutationBuilder
 *       interfaces, not concrete builder classes directly.
 */
public class UserDAO {

    private final QueryExecutor executor;

    public UserDAO() throws SQLException {
        this.executor = new QueryExecutor();
    }

    // ── CREATE ───────────────────────────────────────────────────────────────
    /**
     * Inserts a new user and returns the auto-generated primary key.
     */
    public long create(User user) throws SQLException {
        IMutationBuilder qb = new InsertQueryBuilder()
                .into("users")
                .set("name",  user.getName())
                .set("email", user.getEmail())
                .set("age",   user.getAge())
                .set("city",  user.getCity());
        return executor.executeInsertGetKey(qb.build(), qb.getValues());
    }

    // ── READ ALL ─────────────────────────────────────────────────────────────
    /**
     * Returns all users ordered by id ascending.
     */
    public List<Map<String, Object>> findAll() throws SQLException {
        IQueryBuilder qb = new SelectQueryBuilder()
                .select("*")
                .from("users")
                .orderBy("id", "ASC");
        return executor.executeQuery(qb.build());
    }

    // ── READ BY ID ───────────────────────────────────────────────────────────
    /**
     * Returns a single user by primary key, or null if not found.
     */
    public Map<String, Object> findById(int id) throws SQLException {
        IQueryBuilder qb = new SelectQueryBuilder()
                .select("*")
                .from("users")
                .where("id = ?");
        List<Map<String, Object>> rows = executor.executeQuery(qb.build(), id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    // ── READ BY CITY ─────────────────────────────────────────────────────────
    /**
     * Returns all users in a given city, ordered by name.
     */
    public List<Map<String, Object>> findByCity(String city) throws SQLException {
        IQueryBuilder qb = new SelectQueryBuilder()
                .select("id", "name", "email", "age")
                .from("users")
                .where("city = ?")
                .orderBy("name", "ASC");
        return executor.executeQuery(qb.build(), city);
    }

    // ── READ BY AGE RANGE ────────────────────────────────────────────────────
    /**
     * Returns all users whose age falls within [minAge, maxAge].
     */
    public List<Map<String, Object>> findByAgeRange(int minAge, int maxAge)
            throws SQLException {
        IQueryBuilder qb = new SelectQueryBuilder()
                .select("id", "name", "email", "age", "city")
                .from("users")
                .where("age >= ?")
                .and("age <= ?")
                .orderBy("age", "ASC");
        return executor.executeQuery(qb.build(), minAge, maxAge);
    }

    // ── SEARCH BY NAME (LIKE) ─────────────────────────────────────────────────
    /**
     * Returns users whose name contains the given substring (case-insensitive).
     * The % wildcards are added here so the caller passes a plain name fragment.
     */
    public List<Map<String, Object>> searchByName(String namePart) throws SQLException {
        IQueryBuilder qb = new SelectQueryBuilder()
                .select("id", "name", "email", "city")
                .from("users")
                .where("name LIKE ?")
                .orderBy("name", "ASC");
        return executor.executeQuery(qb.build(), "%" + namePart + "%");
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    /**
     * Updates all mutable fields of the given user. Matches by id.
     * Returns number of rows affected (0 = no user with that id existed).
     */
    public int update(User user) throws SQLException {
        IMutationBuilder qb = new UpdateQueryBuilder()
                .update("users")
                .set("name",  user.getName())
                .set("email", user.getEmail())
                .set("age",   user.getAge())
                .set("city",  user.getCity())
                .where("id = ?", user.getId());
        return executor.executeUpdate(qb.build(), qb.getValues());
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    /**
     * Deletes the user with the given id.
     * Returns number of rows affected.
     */
    public int delete(int id) throws SQLException {
        IMutationBuilder qb = new DeleteQueryBuilder()
                .from("users")
                .where("id = ?", id);
        return executor.executeUpdate(qb.build(), qb.getValues());
    }

    // ── COUNT ─────────────────────────────────────────────────────────────────
    /**
     * Returns the total number of users in the table.
     */
    public long count() throws SQLException {
        IQueryBuilder qb = new SelectQueryBuilder()
                .select("COUNT(*) AS total")
                .from("users");
        List<Map<String, Object>> rows = executor.executeQuery(qb.build());
        return rows.isEmpty() ? 0L : ((Number) rows.get(0).get("total")).longValue();
    }

    // ── DISTINCT CITIES ───────────────────────────────────────────────────────
    /**
     * Returns every unique city where we have at least one user.
     */
    public List<Map<String, Object>> findDistinctCities() throws SQLException {
        IQueryBuilder qb = new SelectQueryBuilder()
                .distinct()
                .select("city")
                .from("users")
                .where("city IS NOT NULL")
                .orderBy("city", "ASC");
        return executor.executeQuery(qb.build());
    }
}
