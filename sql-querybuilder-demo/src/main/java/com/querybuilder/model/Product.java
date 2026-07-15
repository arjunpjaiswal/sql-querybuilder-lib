package com.querybuilder.model;

import java.math.BigDecimal;

/**
 * Product — Plain Java Object (POJO) representing the products table.
 */
public class Product {

    private int        id;
    private String     name;
    private BigDecimal price;
    private String     category;
    private int        stock;

    public Product() {}

    public Product(String name, BigDecimal price, String category, int stock) {
        this.name     = name;
        this.price    = price;
        this.category = category;
        this.stock    = stock;
    }

    public Product(int id, String name, BigDecimal price, String category, int stock) {
        this.id       = id;
        this.name     = name;
        this.price    = price;
        this.category = category;
        this.stock    = stock;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public int        getId()       { return id;       }
    public String     getName()     { return name;     }
    public BigDecimal getPrice()    { return price;    }
    public String     getCategory() { return category; }
    public int        getStock()    { return stock;    }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setId(int id)             { this.id       = id;       }
    public void setName(String name)      { this.name     = name;     }
    public void setPrice(BigDecimal p)    { this.price    = p;        }
    public void setCategory(String cat)   { this.category = cat;      }
    public void setStock(int stock)       { this.stock    = stock;    }

    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', price=" + price +
               ", category='" + category + "', stock=" + stock + "}";
    }
}
