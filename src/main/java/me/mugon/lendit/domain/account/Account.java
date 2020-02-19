package me.mugon.lendit.domain.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.mugon.lendit.domain.Order;

import javax.persistence.*;
import java.util.List;

@Getter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column
    private Long balance;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "account")
    private List<Order> orderList;
}