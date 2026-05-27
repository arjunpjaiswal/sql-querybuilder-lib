package com.querybuilder.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order — Plain Java Object (POJO) representing the orders table.
 */
public class Order {

    private int           id;
    private int           userId;
    private int           productId;
    private int           quantity;
    private BigDecimal    totalPrice;
    private LocalDateTime orderDate;

    public Order() {}

    public Order(int userId, int productId, int quantity, BigDecimal totalPrice) {
        this.userId    = userId;
        this.productId = productId;
        this.quantity  = quantity;
        this.totalPrice = totalPrice;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public int           getId()         { return id;         }
    public int           getUserId()     { return userId;     }
    public int           getProductId()  { return productId;  }
    public int           getQuantity()   { return quantity;   }
    public BigDecimal    getTotalPrice() { return totalPrice; }
    public LocalDateTime getOrderDate()  { return orderDate;  }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setId(int id)                       { this.id         = id;         }
    public void setUserId(int userId)               { this.userId     = userId;     }
    public void setProductId(int productId)         { this.productId  = productId;  }
    public void setQuantity(int quantity)           { this.quantity   = quantity;   }
    public void setTotalPrice(BigDecimal totalPrice){ this.totalPrice  = totalPrice; }
    public void setOrderDate(LocalDateTime date)    { this.orderDate  = date;       }

    @Override
    public String toString() {
        return "Order{id=" + id + ", userId=" + userId + ", productId=" + productId +
               ", quantity=" + quantity + ", totalPrice=" + totalPrice +
               ", orderDate=" + orderDate + "}";
    }
}
