package me.mugon.lendit.domain.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.product.Product;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 유저(Account)와 상품(Product)의 연결 테이블
 * 주문에는 주문 시간과 주문한 사람, 총 금액이 들어가야 하기 때문에 다음과 같이 테이블을 생성함.
 */
@Getter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
public class Orders {

    /** 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 총 금액 */
    private Long total;

    /** 주문할 상품 개수 */
    private Long number;

    /** 주문 시간 */
    private LocalDateTime createdAt;

    /**
     * 유저가 주문한 상품
     * 하나의 상품을 다수의 유저가 주문할 수 있기 때문에 ManyToOne으로 설정
     */
    @ManyToOne
    private Product product;

    /**
     * 주문한 유저
     * 하나의 유저가 여러 개의 상품을 주문할 수 있기 때문에 ManyToOne으로 설정
     */
    @ManyToOne
    private Account account;
}