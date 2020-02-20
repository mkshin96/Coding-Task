package me.mugon.lendit.web.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.order.Orders;
import me.mugon.lendit.domain.product.Product;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrdersRequestDto {

    private Long total;

    private LocalDateTime createdAt;

    private Long number;

    public Orders toEntity(Account currentUser, Product product) {
        return Orders.builder()
                .total(total)
                .number(number)
                .account(currentUser)
                .product(product)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public boolean verifyBalance(Account currentUser) {
        System.out.println("================================");
        System.out.println(currentUser);
        System.out.println(currentUser.getBalance());
        System.out.println(total);
        System.out.println("================================");

        return currentUser.getBalance() < total;
    }

    public boolean verifyAmount(Product product) {
        return product.getAmount() < number;
    }
}
