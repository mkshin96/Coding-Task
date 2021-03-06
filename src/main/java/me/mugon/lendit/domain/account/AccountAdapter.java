package me.mugon.lendit.domain.account;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @AuthenticationPrincipal의 메타 어노테이션인 @CurrentUser을 사용하여 Account 값을 주입받기 위해 선언
 */
@Getter
public class AccountAdapter extends User {

    private Account account;

    public AccountAdapter(Account account) {
        super(account.getUsername(), account.getPassword(), authorities(account));
        this.account = account;
    }

    private static Collection<? extends GrantedAuthority> authorities(Account account) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(account.getRole().name()));
        return authorities;
    }
}
