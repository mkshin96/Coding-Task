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
import org.springframework.hateoas.Link;
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

    /**
     * 유저 생성
     * 1. 클라이언트에게 전달받은 Dto의 username으로 db를 검색
     * 2. db에 있다면 Body에 'message: 중복된유저입니다.' 를 실어서 Bad Request와 함께 반환
     * 3. db에 없다면 저장
     * 4. HATEOAS를 위해 login, self 관계를 EntityModel에 더함
     * 5. Self Descriptive Message를 위해 API Guide의 주소를 profile 관계로 명시하여 더함
     * 6. Header의 Location옵션에 생성된 유저를 조회할 수 있는 링크를 담고, Body에 위의 EntityModel을 실어 반환
     */
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
        accountResource.add(new Link("https://mkshin96.github.io/Coding-Task/#resources-accounts-create").withRel("profile"));
        return ResponseEntity.created(selfLinkBuilder.toUri()).body(accountResource);
    }

    /**
     * 유저 수정
     * 1. url경로로 전달받은 유저의 id로 db를 검색
     * 2. db에 없다면 Body에 'message: 사용자를 찾을 수 없습니다.' 를 실어서 Bad Request와 함께 반환
     * 3. db에 있다면 전달받은 dto의 값으로 데이터 변경
     * 4. HATEOAS를 위해 delete-acoount, self, query-products 관계를 EntityModel에 더함
     * 5. Self Descriptive Message를 위해 API Guide의 주소를 profile 관계로 명시하여 더함
     * 6. Body에 위의 EntityModel을 실어 반환
     */
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
        accountResource.add(new Link("https://mkshin96.github.io/Coding-Task/#resources-accounts-update").withRel("profile"));
        return new ResponseEntity<>(accountResource, HttpStatus.OK);
    }

    /**
     * 유저 삭제
     * 1. url경로로 전달받은 유저의 id로 db를 검색
     * 2. db에 없다면 Body에 'message: 사용자를 찾을 수 없습니다.' 를 실어서 Bad Request와 함께 반환
     * 3. db에 있다면 삭제
     * 4. HATEOAS를 위해 login, self 관계를 EntityModel에 더함
     * 5. Self Descriptive Message를 위해 API Guide의 주소를 profile 관계로 명시하여 더함
     * 6. Body에 위의 EntityModel을 실어 반환
     */
    @Transactional
    public ResponseEntity<?> deleteAccount(Long accountId) {
        Optional<Account> optionalAccount = findById(accountId);
        if (!optionalAccount.isPresent()) {
            return new ResponseEntity<>(baseValidator.returnErrorMessage(USERNOTFOUND), HttpStatus.BAD_REQUEST);
        }
        accountRepository.delete(optionalAccount.get());
        AccountResource accountResource = new AccountResource(new AccountResponseDto(optionalAccount.get()));
        accountResource.add(linkTo(LoginController.class).withRel("login"));
        accountResource.add(new Link("https://mkshin96.github.io/Coding-Task/#resources-accounts-delete").withRel("profile"));
        return ResponseEntity.ok().body(accountResource);
    }

    /**
     * 개별 유저 조회
     * 1. url경로로 전달받은 유저의 id로 db를 검색
     * 2. db에 없다면 Body에 'message: 사용자를 찾을 수 없습니다.' 를 실어서 Bad Request와 함께 반환
     * 3. db에 있다면 responseDto에 데이터를 매핑해줌
     * 4. HATEOAS를 위해 update-account, delete-account, self, query-products 관계를 EntityModel에 더함
     * 5. Self Descriptive Message를 위해 API Guide의 주소를 profile 관계로 명시하여 더함
     * 6. Body에 위의 EntityModel을 실어 반환
     */
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
        accountResource.add(new Link("https://mkshin96.github.io/Coding-Task/#resources-account-get").withRel("profile"));
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
