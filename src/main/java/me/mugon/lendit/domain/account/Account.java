package me.mugon.lendit.domain.account;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import me.mugon.lendit.domain.order.Orders;
import me.mugon.lendit.domain.product.Product;
import me.mugon.lendit.web.dto.account.AccountRequestDto;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author 신무곤
 */
@Getter @NoArgsConstructor @AllArgsConstructor @Builder @EqualsAndHashCode(of = "id")
@Entity @JsonIdentityInfo(scope = Account.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Account {

    /** 유저 테이블의 식별자로 사용될 변수 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 유저가 사용할 이름을 담을 변수 */
    @Column(nullable = false)
    private String username;

    /**
     * 유저가 사용할 비밀번호를 담을 변수
     * 클라이언트에게 반환되면 안되는 중요한 데이터이기 때문에 @JsonIgnore 사용
     */
    @Column
    @JsonIgnore
    private String password;

    /** 유저의 예치금 */
    @Column
    private Long balance;

    /** 유저 역할
     *  현재는 {Role.USER}만 설정 가능
     */
    @Column
    @Enumerated(EnumType.STRING)
    private Role role;

    /** 유저 생성 일시 */
    private LocalDateTime createdAt;

    /**
     * 유저의 주문리스트
     * 하나의 유저는 여러 개의 주문을 할 수 있기 때문에 주문(Orders) 테이블과 OneToMany 로 설정
     * 많은 값이 들어있는 경우 반환 시 비용이 크기 때문에 @JsonIgnore 사용
     */
    @JsonIgnore
    @OneToMany(mappedBy = "account", fetch = FetchType.EAGER)
    private Set<Orders> ordersSet;

    /**
     * 유저가 등록한 상품리스트
     * 많은 값이 들어있는 경우 반환 시 비용이 크기 때문에 @JsonIgnore 사용
     */
    @JsonIgnore
    @OneToMany(mappedBy = "account", fetch = FetchType.EAGER)
    private Set<Product> productSet;

    public void update(AccountRequestDto requestDto) {
        this.username = requestDto.getUsername();
        this.balance = requestDto.getBalance();
    }

    public void reduceBalance(Long total) {
        this.balance -= total;
    }
}