package me.mugon.lendit.web.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import me.mugon.lendit.domain.Order;
import me.mugon.lendit.domain.account.Account;

import java.time.LocalDateTime;
import java.util.List;

@Getter @AllArgsConstructor @Builder
public class AccountResponseDto {

    private Long id;

    private String username;

    private Long balance;

    private List<Order> orderList;

    private LocalDateTime createdAt;

    public AccountResponseDto(Account account) {
        this.id = account.getId();
        this.username = account.getUsername();
        this.balance = account.getBalance();
        this.createdAt = account.getCreatedAt();
    }
}
