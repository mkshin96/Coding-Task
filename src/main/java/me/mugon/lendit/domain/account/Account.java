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

@Getter @NoArgsConstructor @AllArgsConstructor @Builder @EqualsAndHashCode(of = "id") @ToString
@Entity @JsonIdentityInfo(scope = Account.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column
    @JsonIgnore
    private String password;

    @Column
    private Long balance;

    @Column
    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDateTime createdAt;

    @JsonIgnore
    @OneToMany(mappedBy = "account", fetch = FetchType.EAGER)
    private Set<Orders> ordersSet;

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