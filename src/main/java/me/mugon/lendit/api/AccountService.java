package me.mugon.lendit.api;

import lombok.RequiredArgsConstructor;
import me.mugon.lendit.domain.common.BaseValidator;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.account.AccountAdapter;
import me.mugon.lendit.domain.account.AccountRepository;
import me.mugon.lendit.domain.account.AccountResource;
import me.mugon.lendit.web.AccountController;
import me.mugon.lendit.web.LoginController;
import me.mugon.lendit.web.ProductController;
import me.mugon.lendit.web.dto.account.AccountRequestDto;
import me.mugon.lendit.web.dto.account.AccountResponseDto;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static me.mugon.lendit.api.error.ErrorMessageConstant.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RequiredArgsConstructor
@Service
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;

    private final BaseValidator baseValidator;

    @Transactional
    public ResponseEntity<?> saveAccount(AccountRequestDto requestDto) {
        Optional<Account> findByUsername = accountRepository.findByUsername(requestDto.getUsername());
        if (findByUsername.isPresent()) {
            return new ResponseEntity<>(baseValidator.returnErrorMessage(DUPLICATEDUSER), HttpStatus.BAD_REQUEST);
        }
        Account savedAccount = accountRepository.save(requestDto.toEntity());
        WebMvcLinkBuilder selfLinkBuilder = linkTo(AccountController.class).slash(savedAccount.getId());
        AccountResponseDto accountResponseDto = new AccountResponseDto(savedAccount);
        AccountResource accountResource = new AccountResource(accountResponseDto);
        accountResource.add(linkTo(LoginController.class).withRel("login"));
        return ResponseEntity.created(selfLinkBuilder.toUri()).body(accountResource);
    }

    @Transactional
    public ResponseEntity<?> updateAccount(Long accountId, AccountRequestDto requestDto) {
        Optional<Account> optionalAccount = findById(accountId);
        if (!optionalAccount.isPresent()) {
            return new ResponseEntity<>(baseValidator.returnErrorMessage(USERNOTFOUND), HttpStatus.BAD_REQUEST);
        }
        Account account = optionalAccount.get();
        account.update(requestDto);
        AccountResponseDto accountResponseDto = new AccountResponseDto(account);
        AccountResource accountResource = new AccountResource(accountResponseDto);
        accountResource.add(linkTo(AccountController.class).slash(accountResponseDto.getId()).withRel("delete-account"));
        accountResource.add(linkTo(ProductController.class).withRel("query-products"));
        return new ResponseEntity<>(accountResource, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> deleteAccount(Long accountId) {
        Optional<Account> optionalAccount = findById(accountId);
        if (!optionalAccount.isPresent()) {
            return new ResponseEntity<>(baseValidator.returnErrorMessage(USERNOTFOUND), HttpStatus.BAD_REQUEST);
        }
        accountRepository.delete(optionalAccount.get());
        AccountResource accountResource = new AccountResource(new AccountResponseDto(optionalAccount.get()));
        accountResource.add(linkTo(LoginController.class).withRel("login"));
        return ResponseEntity.ok().body(accountResource);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getAccount(Long accountId) {
        Optional<Account> optionalAccount = findById(accountId);
        if (!optionalAccount.isPresent()) {
            return new ResponseEntity<>(baseValidator.returnErrorMessage(USERNOTFOUND), HttpStatus.BAD_REQUEST);
        }
        Account account = optionalAccount.get();
        AccountResource accountResource = new AccountResource(new AccountResponseDto(account));
        accountResource.add(linkTo(AccountController.class).slash(accountId).withRel("update-account"));
        accountResource.add(linkTo(AccountController.class).slash(accountId).withRel("delete-account"));
        accountResource.add(linkTo(ProductController.class).withRel("query-products"));
        return new ResponseEntity<>(accountResource, HttpStatus.OK);
    }

    public Optional<Account> findById(Long accountId) {
        return accountRepository.findById(accountId);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.accountRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username + "을 찾을 수 없습니다."));
        return new AccountAdapter(account);
    }
}
