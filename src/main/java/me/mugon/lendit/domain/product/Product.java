package me.mugon.lendit.domain.product;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.order.Orders;
import me.mugon.lendit.web.dto.product.ProductRequestDto;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @JsonIdentityInfo(scope = Product.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Long amount; //수량

    private LocalDateTime createdAt;

    private boolean checkAmount;

    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER)
    private List<Orders> ordersList;

    @ManyToOne
    private Account account;

    public void update(ProductRequestDto requestDto) {
        this.name = requestDto.getName();
        this.price = requestDto.getPrice();
        this.amount = requestDto.getAmount();
    }

    public void reduceAmount(Long number) {
        this.amount -= number;
    }

    public void mapUser(Account currentUser) {
        if (this.account != null) {
            this.account.getProductSet().remove(this);
        }
        this.account = currentUser;
        this.account.getProductSet().add(this);
    }

    public void amountIsZero() {
        this.checkAmount = true;
    }
}