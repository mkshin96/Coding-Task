package me.mugon.lendit.web.dto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.order.Orders;
import me.mugon.lendit.domain.product.Product;

import java.time.LocalDateTime;

@Getter @AllArgsConstructor
public class OrdersResponseDto {

    private Long id;

    private Long total;

    private Long number;

    private LocalDateTime createdAt;

    private Product product;

    private Account account;

    public OrdersResponseDto(Orders orders) {
        this.id = orders.getId();
        this.total = orders.getTotal();
        this.number = orders.getNumber();
        this.createdAt = orders.getCreatedAt();
        this.product = orders.getProduct();
        this.account = orders.getAccount();
    }
}
