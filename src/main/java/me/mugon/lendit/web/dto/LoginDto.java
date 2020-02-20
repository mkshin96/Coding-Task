package me.mugon.lendit.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.mugon.lendit.domain.account.Account;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor @AllArgsConstructor @Builder
public class LoginDto {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    public Account toEntity() {
        return Account.builder()
                .username(username)
                .password(password)
                .build();
    }
}
