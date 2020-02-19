package me.mugon.lendit.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.product.Product;

import javax.persistence.*;
import java.util.List;

@Getter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany
    private List<Product> productList;

    @ManyToOne
    private Account account;
}