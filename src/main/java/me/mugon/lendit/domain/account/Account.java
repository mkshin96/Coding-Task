package me.mugon.lendit.domain.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.mugon.lendit.domain.Order;
import me.mugon.lendit.web.dto.account.AccountRequestDto;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column
    private Long balance;

    private LocalDateTime createdAt;

//    @OneToMany(fetch = FetchType.EAGER, mappedBy = "account")
//    private List<Order> orderList;

    public void update(AccountRequestDto requestDto) {
        this.username = requestDto.getUsername();
        this.balance = requestDto.getBalance();
    }
}