package me.mugon.lendit.web.dto.order;

import lombok.*;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.order.Orders;
import me.mugon.lendit.domain.product.Product;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrdersRequestDto {

    @NotNull(message = "please enter total price")
    private Long total;

    @NotNull(message = "please enter product number")
    private Long number;

    @NotNull(message = "please enter product identifier")
    private Long productId;

    public boolean verifyBalance(Account currentUser) {
        return currentUser.getBalance() < total;
    }

    public boolean verifyAmount(Product product) {
        return product.getAmount() < number;
    }

    public Orders toEntity(Account currentUser, Product savedProduct) {
        return Orders.builder()
                .total(total)
                .number(number)
                .account(currentUser)
                .product(savedProduct)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
