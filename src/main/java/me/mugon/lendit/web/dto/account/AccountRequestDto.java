package me.mugon.lendit.web.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.mugon.lendit.domain.account.Account;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AccountRequestDto {

    @NotBlank
    private String username;

    private Long balance;

    public Account toEntity() {
        return Account.builder()
                .username(username)
                .balance(balance)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
