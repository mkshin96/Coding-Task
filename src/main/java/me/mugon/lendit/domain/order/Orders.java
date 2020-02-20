package me.mugon.lendit.domain.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.product.Product;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //총 금액
    private Long total;

    //상품 개수
    private Long number;

    private LocalDateTime createdAt;

    @ManyToOne
    private Product product;

    @ManyToOne
    private Account account;

    public void map() {
//        if (this.account.getOrdersSet() == null) {
//
//        }
//        this.account.getOrderList().add(this);
//        this.product.getOrdersList().add(this);
    }
}