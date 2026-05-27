package com.querybuilder.model;

/**
 * User — Plain Java Object (POJO) representing the users table.
 *
 * Intentionally free of any SQL or JDBC logic — that responsibility
 * belongs to UserDAO.
 */
public class User {

    private int    id;
    private String name;
    private String email;
    private int    age;
    private String city;

    /** Default constructor (required for some frameworks) */
    public User() {}

    /** Constructor for creating a new user (id not yet known) */
    public User(String name, String email, int age, String city) {
        this.name  = name;
        this.email = email;
        this.age   = age;
        this.city  = city;
    }

    /** Full constructor including id (for updating existing records) */
    public User(int id, String name, String email, int age, String city) {
        this.id    = id;
        this.name  = name;
        this.email = email;
        this.age   = age;
        this.city  = city;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public int    getId()    { return id;    }
    public String getName()  { return name;  }
    public String getEmail() { return email; }
    public int    getAge()   { return age;   }
    public String getCity()  { return city;  }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setId(int id)       { this.id    = id;    }
    public void setName(String n)   { this.name  = n;     }
    public void setEmail(String e)  { this.email = e;     }
    public void setAge(int a)       { this.age   = a;     }
    public void setCity(String c)   { this.city  = c;     }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', email='" + email +
               "', age=" + age + ", city='" + city + "'}";
    }
}
