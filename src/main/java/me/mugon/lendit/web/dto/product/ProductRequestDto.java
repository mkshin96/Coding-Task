package me.mugon.lendit.web.dto.product;

import lombok.*;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.product.Product;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ProductRequestDto {

    @NotBlank
    private String name;

    @NotNull
    private Long price;

    @NotNull
    private Long amount;

    public Product toEntity(Account currentUser) {
        return Product.builder()
                .name(name)
                .price(price)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
