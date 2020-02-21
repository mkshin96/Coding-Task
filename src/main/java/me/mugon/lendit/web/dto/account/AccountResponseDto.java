package me.mugon.lendit.web.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.order.Orders;
import me.mugon.lendit.domain.product.Product;

import java.time.LocalDateTime;
import java.util.Set;

@Getter @AllArgsConstructor @Builder
public class AccountResponseDto {

    private Long id;

    private String username;

    private Long balance;

    private Set<Orders> ordersSet;

    private Set<Product> productSet;

    private LocalDateTime createdAt;

    public AccountResponseDto(Account account) {
        this.id = account.getId();
        this.username = account.getUsername();
        this.balance = account.getBalance();
        this.createdAt = account.getCreatedAt();
        this.ordersSet = account.getOrdersSet();
        this.productSet = account.getProductSet();
    }
}
