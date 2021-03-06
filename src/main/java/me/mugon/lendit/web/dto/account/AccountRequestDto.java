package me.mugon.lendit.web.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.account.Role;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AccountRequestDto {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private Long balance;

    public Account toEntity() {
        return Account.builder()
                .username(username)
                .password(password)
                .balance(balance)
                .role(Role.ROLE_USER)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
