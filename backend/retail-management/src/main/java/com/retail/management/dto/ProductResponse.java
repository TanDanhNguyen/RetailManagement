
package com.retail.management.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductResponse {

    private Long id;
    private String name;
    private String sku;
    private BigDecimal price;
    private Integer stockQuantity;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private Long version;

    public ProductResponse() {
    }

    public ProductResponse(
            Long id,
            String name,
            String sku,
            BigDecimal price,
            Integer stockQuantity,
            Boolean isActive,
            LocalDateTime createdAt,
            Long version) {

        this.id = id;
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.version = version;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSku() { return sku; }
    public BigDecimal getPrice() { return price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public Boolean getIsActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getVersion() {
        return version;
    }
}
