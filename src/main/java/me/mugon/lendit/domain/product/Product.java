package me.mugon.lendit.domain.product;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.order.Orders;
import me.mugon.lendit.web.dto.product.ProductRequestDto;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 상품 테이블
 */
@Getter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @JsonIdentityInfo(scope = Product.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Product implements Serializable {

    /** 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 상품 이름 */
    @Column(nullable = false)
    private String name;

    /** 상품 가격 */
    @Column(nullable = false)
    private Long price;

    /** 상품 재고 수량 */
    @Column(nullable = false)
    private Long amount; //수량

    /** 상품 등록 일시 */
    private LocalDateTime createdAt;

    /** 상품이 주문 가능한 상태인지(false = 주문 가능) */
    @JsonIgnore
    private boolean checkAmount;

    /** 상품의 주문리스트 */
    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER)
    private List<Orders> ordersList;

    /** 상품을 등록한 유저 */
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

    public boolean amountEqualsZero() {
        return this.amount == 0;
    }

    public void changeCheckAmount() {
        this.checkAmount = true;
    }
}