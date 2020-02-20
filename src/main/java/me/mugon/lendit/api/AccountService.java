package me.mugon.lendit.api;

import lombok.RequiredArgsConstructor;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.account.AccountAdapter;
import me.mugon.lendit.domain.account.AccountRepository;
import me.mugon.lendit.web.dto.account.AccountRequestDto;
import me.mugon.lendit.web.dto.account.AccountResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static me.mugon.lendit.api.error.ErrorMessageConstant.*;

@RequiredArgsConstructor
@Service
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Transactional
    public ResponseEntity<?> saveAccount(AccountRequestDto requestDto) {
        Optional<Account> findByUsername = accountRepository.findByUsername(requestDto.getUsername());
        if (findByUsername.isPresent()) {
            return new ResponseEntity<>(getErrorMap(DUPLICATEDUSER), HttpStatus.BAD_REQUEST);
        }
        Account savedAccount = accountRepository.save(requestDto.toEntity());
        return new ResponseEntity<>(new AccountResponseDto(savedAccount), HttpStatus.CREATED);
    }

    @Transactional
    public ResponseEntity<?> updateAccount(Long accountId, AccountRequestDto requestDto) {
        Optional<Account> optionalAccount = findById(accountId);
        if (!optionalAccount.isPresent()) {
            return new ResponseEntity<>(getErrorMap(USERNOTFOUND), HttpStatus.BAD_REQUEST);
        }
        Account account = optionalAccount.get();
        account.update(requestDto);
        return new ResponseEntity<>(new AccountResponseDto(account), HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> deleteAccount(Long accountId) {
        Optional<Account> optionalAccount = findById(accountId);
        if (!optionalAccount.isPresent()) {
            return new ResponseEntity<>(getErrorMap(USERNOTFOUND), HttpStatus.BAD_REQUEST);
        }
        accountRepository.delete(optionalAccount.get());
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> getAccount(Long accountId) {
        Optional<Account> optionalAccount = findById(accountId);
        if (!optionalAccount.isPresent()) {
            return new ResponseEntity<>(getErrorMap(USERNOTFOUND), HttpStatus.BAD_REQUEST);
        }
        Account account = optionalAccount.get();
        return new ResponseEntity<>(new AccountResponseDto(account), HttpStatus.OK);
    }

    public Optional<Account> findById(Long accountId) {
        return accountRepository.findById(accountId);
    }

    private Map<String, List<String>> getErrorMap(String message) {
        Map<String, List<String>> errors = new HashMap<>();
        errors.put(KEY, Arrays.asList(message));
        return errors;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.accountRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username + "을 찾을 수 없습니다."));
        return new AccountAdapter(account);
    }
}
