package me.mugon.lendit.web.dto;

import lombok.Getter;
import me.mugon.lendit.domain.product.Product;

import java.time.LocalDateTime;

@Getter
public class ProductResponseDto {

    private Long id;

    private String name;

    private Long price;

    private Long amount;

    private LocalDateTime createdAt;

    public ProductResponseDto(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.amount = product.getAmount();
        this.createdAt = product.getCreatedAt();
    }
}
